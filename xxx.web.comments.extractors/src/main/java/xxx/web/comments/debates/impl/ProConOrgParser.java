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

package xxx.web.comments.debates.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import xxx.web.comments.Utils;
import xxx.web.comments.createdebate.Argument;
import xxx.web.comments.createdebate.Debate;
import xxx.web.comments.debates.DebateParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ivan Habernal
 */
public class ProConOrgParser
        implements DebateParser
{
    @Override
    public Debate parseDebate(InputStream inputStream)
            throws IOException
    {
        Debate result = new Debate();

        Document doc = Jsoup.parse(inputStream, "UTF-8", "http://www.procon.org/");

        // Set the Url of the doc

        // title
        Element body = doc.body();
        Elements debateTitleElements = body.select("p[class=title]").select("p[style]");

        if (debateTitleElements.first() == null) {
            // not a debate
            return null;
        }

        String title = Utils.normalize(debateTitleElements.first().text());
        result.setTitle(title);

        Element trAnswers = body.select("tr > td > b:contains(PRO \\(yes\\))").parents().first()
                .parents().first().nextElementSibling();

        // the PRO side
        Element proTd = trAnswers.select("td").get(0);
        Element conTd = trAnswers.select("td").get(1);

        System.out.println(proTd.select("blockquote").size());
        System.out.println(conTd.select("blockquote").size());

        Elements texts = proTd.select("blockquote > div[class=editortext]");
        for (Element text : texts) {
            Argument argument = new Argument();
            argument.setStance("pro");
            argument.setText(extractPlainTextFromTextElement(text));

            result.getArgumentList().add(argument);
        }

        texts = conTd.select("blockquote > div[class=editortext]");
        for (Element text : texts) {
            Argument argument = new Argument();
            argument.setStance("con");
            argument.setText(extractPlainTextFromTextElement(text));

            result.getArgumentList().add(argument);
        }

        return result;
    }

    /**
     * Extracts the document of the quote
     *
     * @param textElement text quote element
     * @return plain string with paragraphs kept
     */
    protected static String extractPlainTextFromTextElement(Element textElement)
    {
        StringBuilder sb = new StringBuilder();

        for (Node childNode : textElement.childNodes()) {
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;

                String tagName = childElement.tagName();

                if ("p".equals(tagName) || "span".equals(tagName)) {
                    sb.append(childElement.text());
                    sb.append("\n");
                }
                else if ("br".equals(tagName)) {
                    // prevent double newlines
                    sb = new StringBuilder(sb.toString().trim());
                    sb.append("\n");
                }

            }
            else if (childNode instanceof TextNode) {
                TextNode textNode = (TextNode) childNode;

                sb.append(textNode.text());
            }
        }

        // remove leading + ending quotes
        return Utils.normalize(sb.toString()).replaceAll("[(^\")(\"$)]", "");
    }
}
