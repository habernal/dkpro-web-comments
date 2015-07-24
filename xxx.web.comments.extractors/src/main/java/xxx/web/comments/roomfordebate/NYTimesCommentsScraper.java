/*
 * Copyright 2015 XXX
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

package xxx.web.comments.roomfordebate;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import xxx.web.comments.Article;
import xxx.web.comments.Comment;
import xxx.web.comments.Utils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scraping comments to Room for Debate articles
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

    /**
     * Extracts comments from the input html stream
     *
     * @param inputStream stream
     * @return list of comments (never null)
     * @throws IOException exception
     */
    public List<Comment> extractComments(InputStream inputStream)
            throws IOException
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
                        String dateText = childElement.select("a.comment-time").text();
                        try {
                            Date date = df.parse(dateText);
                            comment.setTimestamp(date);
                        }
                        catch (ParseException e) {
                            // maybe it's "x days ago"
                            Pattern p = Pattern.compile("(\\d+) days ago");
                            Matcher m = p.matcher(dateText);
                            while (m.find()) {
                                // get the value
                                int xDaysAgo = Integer.valueOf(m.group(1));

                                // translate to Java date
                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.DAY_OF_YEAR, (-xDaysAgo));
                                Date date = cal.getTime();

                                comment.setTimestamp(date);
                            }
                        }
                    }
                    // recommendations
                    else if ("footer".equals(childElement.nodeName())) {
                        Elements select = childElement.select("span.recommend-count");
                        if (!select.text().isEmpty()) {
                            comment.setRecommendCount(Integer.valueOf(select.text()));
                        }
                    }
                    // the text
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
            String links =
                    "http://www.nytimes.com/roomfordebate/2015/05/20/same-sex-marriage-and-the-future-of-irish-catholicism/irish-catholicism-can-adapt-to-a-new-role\n"
                            + "http://www.nytimes.com/roomfordebate/2015/05/20/same-sex-marriage-and-the-future-of-irish-catholicism/the-catholic-church-has-already-evolved-in-ireland-thanks-to-women\n"
                            + "http://www.nytimes.com/roomfordebate/2015/05/20/same-sex-marriage-and-the-future-of-irish-catholicism/irelands-catholic-church-lost-its-moral-authority-a-while-ago-1\n"
                            + "http://www.nytimes.com/roomfordebate/2015/05/20/same-sex-marriage-and-the-future-of-irish-catholicism/theres-a-new-generation-of-irish-catholics-in-ireland\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/24/besides-the-confederate-flag-what-other-symbols-should-go/lets-focus-instead-policies-that-enforce-structural-racism\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/24/besides-the-confederate-flag-what-other-symbols-should-go/can-we-please-finally-get-rid-of-aunt-jemima\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/24/besides-the-confederate-flag-what-other-symbols-should-go/bases-named-after-confederates-are-an-insult-to-us-soldiers\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/24/besides-the-confederate-flag-what-other-symbols-should-go/rename-public-schools-that-honor-racists-but-stop-there\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/26/should-happy-hour-be-banned/happy-hour-alternatives-camaraderie-that-doesnt-lead-to-hangovers\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/26/should-happy-hour-be-banned/i-need-to-relax-with-friends-at-the-bar-at-happy-hour\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/26/should-happy-hour-be-banned/responsible-service-makes-sense-happy-hour-bans-dont\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/26/should-happy-hour-be-banned/theres-nothing-happy-about-drunk-driving-deaths\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/30/should-greece-abandon-the-euro/greece-must-leave-the-euro-to-avoid-greater-misery-from-austerity\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/30/should-greece-abandon-the-euro/greece-needs-the-euro-to-stay-competitive\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/30/should-greece-abandon-the-euro/greece-cant-afford-to-leave-the-euro-though-it-should\n"
                            + "http://www.nytimes.com/roomfordebate/2015/06/30/should-greece-abandon-the-euro/the-euro-is-a-straitjacket-for-greece\n"
                            + "http://www.nytimes.com/roomfordebate/2015/07/13/birth-control-on-demand/we-need-more-honesty-about-contraceptives-risks-and-effects\n"
                            + "http://www.nytimes.com/roomfordebate/2015/07/13/birth-control-on-demand/publicly-funded-birth-control-is-crucial\n"
                            + "http://www.nytimes.com/roomfordebate/2015/07/13/birth-control-on-demand/publicly-funded-birth-control-is-crucial\n"
                            + "http://www.nytimes.com/roomfordebate/2015/07/13/birth-control-on-demand/women-need-dignity-more-than-sex-without-consequence";

            for (String link : StringUtils.split(links, "\n")) {
                NYTimesCommentsScraper nyTimesCommentsScraper = new NYTimesCommentsScraper();
                System.out.println(link);

                String html = nyTimesCommentsScraper.readHTML(link);
                //                    "http://www.nytimes.com/roomfordebate/2015/06/30/should-greece-abandon-the-euro/the-euro-is-a-straitjacket-for-greece");
//                File tmpFile = new File("src/test/resources/nytimes-step2.html");
                //            FileUtils.writeStringToFile(tmpFile, html);

                List list = new ArrayList();

                Article article = new NYTimesArticleExtractor().extractArticle(html);

                list.add(article);


                List comments = nyTimesCommentsScraper
                        .extractComments(IOUtils.toInputStream(html, "utf-8"));

                list.addAll(comments);

//                for (Comment comment : comments) {
//                    System.out.println(comment);
//                }

                FileOutputStream fos = new FileOutputStream(File.createTempFile("nyt", ".xml"));
                XStream xStream = new XStream();

                xStream.toXML(list, fos);

                fos.close();
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
