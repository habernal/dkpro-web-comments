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
public class ProConOrgParser implements DebateParser
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

        Element trAnswers = body.select("tr > td > b:contains(PRO \\(yes\\))").parents().first().parents().first().nextElementSibling();

        // the PRO side
        Element proTd = trAnswers.select("td").get(0);
        Element conTd = trAnswers.select("td").get(1);

        System.out.println(proTd.select("blockquote").size());
        System.out.println(conTd.select("blockquote").size());

        Elements texts = proTd.select("blockquote > div[class=editortext]");
        for (Element text: texts) {
            Argument argument = new Argument();
            argument.setStance("pro");
            argument.setText(removeQuotes(text.text()));

            // TODO make sure we parse paragraphs as well

            result.getArgumentList().add(argument);
        }

        texts = conTd.select("blockquote > div[class=editortext]");
        for (Element text: texts) {
            Argument argument = new Argument();
            argument.setStance("con");
            argument.setText(removeQuotes(text.text()));

            result.getArgumentList().add(argument);
        }

        /*
        Element twoSidesAndScores = body
                .select("table[style=margin:0 auto;padding:0;border:0;text-align:center;width:98%;]")
                .first();

        if (twoSidesAndScores == null) {
            // this is not a two-side debate
            return null;
        }

        Elements twoSides = twoSidesAndScores.select("div[class=sideTitle]");
        if (twoSides.size() != 2) {
            // this is not a two-side debate
            return null;
        }

        // description
        StringBuilder debateDescriptionBuilder = new StringBuilder();
        Element description = body.select("#description").first();

        if (description != null) {
            Element descriptionText = description.select("div[class=centered debatelongDesc]")
                    .first();
            if (descriptionText.select("p").isEmpty()) {
                // just extract the text
                debateDescriptionBuilder.append(descriptionText.text());
            }
            else if ("".equals(descriptionText.select("p").first().text())) {
                // extract paragraphs
                for (Element p : descriptionText.select("p").select("span")) {
                    debateDescriptionBuilder.append(p.ownText());
                    debateDescriptionBuilder.append("\n");
                }
            }
            else {
                // extract paragraphs
                for (Element p : descriptionText.select("p")) {
                    debateDescriptionBuilder.append(p.text());
                    debateDescriptionBuilder.append("\n");
                }
            }
        }
        result.setDescription(Utils.normalize(debateDescriptionBuilder.toString()));
        */

        return result;
    }

    private static String removeQuotes(String s)
    {
        return s.replaceAll("[(^\")(\"$)]", "");
    }
}
