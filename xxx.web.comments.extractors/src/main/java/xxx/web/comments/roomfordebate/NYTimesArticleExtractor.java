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

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import xxx.web.comments.Article;
import xxx.web.comments.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * (c) 2015 XXX
 */
public class NYTimesArticleExtractor
{

    public String readHTML(String url)
            throws IOException
    {
        StringWriter writer = new StringWriter();
        IOUtils.copy(new URL(url).openStream(), writer, "utf-8");

        return writer.toString();
    }

    public Article extractArticle(String html)
            throws ParseException, IOException
    {
        Article result = new Article();

        Document doc = Jsoup.parse(html, getBaseName());

        Element element = doc.select("article.rfd").iterator().next();

        //		System.out.println(element);

        String dateText = element.select("p.pubdate").text().replaceAll("Updated[\\s]+", "");
        // time
        try {
            DateFormat df = new SimpleDateFormat("MMM dd, yyyy, hh:mm aaa", Locale.ENGLISH);
            Date date = df.parse(dateText);
            result.setTimestamp(date);
        }
        catch (ParseException e) {
            // June 24, 2015
            DateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            Date date = df.parse(dateText);
            result.setTimestamp(date);
        }

        // title
        result.setTitle(Utils.normalize(element.select("h1").text()));

        // text
        StringBuilder sb = new StringBuilder();
        for (Element p : element.select("div.nytint-post > p")) {
            sb.append(p.text());
            sb.append("\n");
        }
        result.setText(Utils.normalize(sb.toString()));

        // debate title
        result.setDebateTitle(
                Utils.normalize(doc.select("div.nytint-discussion-overview > h2").text()));

        // debate url
        result.setDebateUrl(doc.select("div.nytint-discussion-overview > h2 > a").iterator().next()
                .attr("href"));

        // document url
        result.setUrl(doc.select("meta[name=communityAssetURL]").attr("content"));

        // debate description
        result.setDebateDescription(Utils.normalize(
                ((TextNode) doc.select("div.nytint-discussion-overview > p").iterator()
                        .next()
                        .childNodes().iterator().next()).text()));

        // aurhor
        result.setAuthor(element.select("div.nytint-mugshots > img").iterator().next().attr("alt"));

        // topics
        for (Element a : element.select("p.nytint-tags > a")) {
            result.getTopics().add(a.attr("href"));
        }

        return result;
    }

    public String getBaseName()
    {
        return "www.nytimes.com";
    }
}
