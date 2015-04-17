/*
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.dkpro.web.comments.roomfordebate;

import de.tudarmstadt.ukp.dkpro.web.comments.Comment;
import de.tudarmstadt.ukp.dkpro.web.comments.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Hello world!
 */
public class NYTimesCommentsScraper
{

    /**
     * Downloads the page and rolls out the entire discussion using {@link FirefoxDriver}.
     *
     * @param articleUrl url, e.g. {@code http://www.nytimes.com/roomfordebate/2015/02/04/regulate-internet-providers/the-internet-is-back-to-solid-regulatory-ground}
     * @return generated HTML code of the entire page
     * @throws InterruptedException
     */
    public String readHTML(String articleUrl)
            throws InterruptedException
    {
        // load the url
        WebDriver driver = new FirefoxDriver();
        driver.get(articleUrl);

        // roll-out the entire discussion
        List<WebElement> commentsExpandElements;
        do {
            commentsExpandElements = driver.findElements(By.cssSelector("div.comments-expand"));

            // click on each of them
            for (WebElement commentsExpandElement : commentsExpandElements) {
                // only if visible & enabled
                if (commentsExpandElement.isDisplayed() && commentsExpandElement.isEnabled()) {
                    commentsExpandElement.click();

                    // give it some time to load new comments
                    Thread.sleep(3000);
                }
            }
        }
        // until there is one remaining that doesn't do anything...
        while (commentsExpandElements.size() > 1);

        // get the html
        String result = driver.getPageSource();

        // close firefox
        driver.close();

        return result;
    }

    public List<Comment> extractComments(InputStream inputStream)
            throws IOException, ParseException
    {
        List<Comment> result = new ArrayList<Comment>();

        Document doc = Jsoup.parse(inputStream, "utf-8", getBaseName());

        for (Element element : doc.select("#commentsContainer article")) {
            Comment comment = new Comment();

            // id
            comment.setId(element.attr("data-id"));
            // parent id
            comment.setParentId(!element.attr("data-parentid").equals("0") ?
                    element.attr("data-parentid") :
                    null);

            // previous comment id (if available)
            Comment previousComment = result.isEmpty() ? null : result.get(result.size() - 1);
            // if the previous comment has parent, the current comment is a reaction to the previous one
            if (previousComment != null && previousComment.getParentId() != null) {
                comment.setPreviousPostId(previousComment.getId());
            }

            // now metadata and content
            for (Node child : element.childNodes()) {
                if (child instanceof Element) {
                    Element childElement = (Element) child;

                    if ("header".equals(childElement.nodeName())) {
                        comment.setCommenterName(Utils.normalize(
                                childElement.select("h3.commenter").iterator().next().text()));
                        comment.setCommenterLocation(Utils.normalize(
                                childElement.select("span.commenter-location").iterator().next()
                                        .text()));
                        comment.setCommenterTrusted(
                                childElement.select("i.trusted-icon").size() == 1);
                        // time
                        DateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                        Date date = df.parse(childElement.select("a.comment-time").text());
                        comment.setTimestamp(date);
                    }
                    else if ("footer".equals(childElement.nodeName())) {
                        Elements select = childElement.select("span.recommend-count");
                        if (!select.text().isEmpty()) {
                            comment.setRecommendCount(Integer.valueOf(select.text()));
                        }
                    }
                    else if ("p".equals(childElement.nodeName())) {
                        String text = paragraphElementToString(childElement);

                        // and do some cleaning and normalization
                        String normalized = Utils.normalize(text);

                        comment.setText(normalized);
                    }
                }
            }

            result.add(comment);
        }

        return result;
    }

    /**
     * Extracts elements from the html comments (paragraph breaks, links)
     *
     * @param pElement paragraph element
     * @return plain text
     */
    public String paragraphElementToString(Element pElement)
    {
        StringBuilder sb = new StringBuilder();
        for (Node child : pElement.childNodes()) {
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;

                sb.append(textNode.text());
            }
            else if (child instanceof Element) {
                Element element = (Element) child;

                // append new line for break
                if ("br".equals(element.tag().getName())) {
                    sb.append("\n");
                }
                else if ("a".equals(element.tag().getName())) {
                    // extract link from a.href
                    sb.append(" ").append(element.attr("href")).append(" ");
                }
                else {
                    // or just add the text
                    sb.append(" ").append(element.text()).append(" ");
                }
            }
        }

        return sb.toString();
    }

    public static void main(String[] args)
    {
        try {
            NYTimesCommentsScraper nyTimesCommentsScraper = new NYTimesCommentsScraper();
            //			String html = nyTimesCommentsScraper.readHTML("http://www.nytimes.com/roomfordebate/2015/02/04/regulate-internet-providers/the-internet-is-back-to-solid-regulatory-ground");
            File tmpFile = new File("src/test/resources/nytimes-step2.html");
            //			FileUtils.writeStringToFile(tmpFile, html);

            List<Comment> comments = nyTimesCommentsScraper
                    .extractComments(new FileInputStream(tmpFile));

            for (Comment comment : comments) {
                System.out.println(comment);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBaseName()
    {
        return "www.nytimes.com";
    }
}
