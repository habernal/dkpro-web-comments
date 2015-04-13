package de.tudarmstadt.ukp.dkpro.web.comments.createdebate;

import de.tudarmstadt.ukp.dkpro.web.comments.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsing  debates from {@code createdebate.com} site.
 *
 * @author Ivan Habernal
 * @author Anil Narassiguin
 */

public class CreateDebateHTMLParser
{

    /**
     * Parses a debate HTML site from {@code createdebate.com} and extracts all arguments and
     * debate description.
     *
     * @param inputStream input stream
     * @return debate and arguments or null, if the debate is not parseable
     * @throws IOException
     */
    public static Debate parseDebate(InputStream inputStream)
            throws IOException
    {
        Debate result = new Debate();

        Document doc = Jsoup.parse(inputStream, "UTF-8", "http://www.createdebate.com/");

        // Set the Url of the doc
        result.setUrl(doc.select("link").attr("href"));

        // title
        Element body = doc.body();
        Elements debateTitleElement = body.select("h1[class=debateTitle]");

        if (debateTitleElement.first() == null) {
            // not a debate
            return null;
        }

        String title = Utils.normalize(debateTitleElement.first().text());
        result.setTitle(title);

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

        Element debateSideBoxL = body.select("div[class=debateSideBox sideL]").first();
        Element debateSideBoxR = body.select("div[class=debateSideBox sideR]").first();

        List<Element> debateSideBoxes = new ArrayList<>();
        debateSideBoxes.addAll(debateSideBoxL.select("div[class=argBox argument][id~=arg]"));
        debateSideBoxes.addAll(debateSideBoxR.select("div[class=argBox argument][id~=arg]"));

        for (Element argBody : debateSideBoxes) {
            Argument argumentWithParent = extractArgument(argBody);
            Element parent = argBody.parent();
            Element previousElement = parent.previousElementSibling();
            String parentId = null;
            if (previousElement != null) {
                Element realParent = previousElement.previousElementSibling();
                if (realParent != null) {
                    parentId = realParent.id();
                }
            }

            argumentWithParent.setParentId(parentId);

            result.getArgumentList().add(argumentWithParent);
        }

        return result;
    }

    /**
     * Argument Box (from the {@link Element} class) Example of argument
     * box: <div class="argBox argument" id="arg153614">
     * The goal is to parse html debate pages from the  website http://www.createdebate.com
     * This method extracts argument from elements identified by
     * {@code <div class=argBox argument... } which correspond to one argument
     *
     * @param argBox element
     * @return argument
     */
    public static Argument extractArgument(Element argBox)
    {
        Argument result = new Argument();

        Element name = argBox.select("a[href~=//www.createdebate.com/user/viewprofile/][title]")
                .first();
        result.setAuthor(name.ownText());

        Element argPoints = argBox.select("span[id~=tot.*]").first();
        result.setArgPoints(Integer.parseInt(argPoints.ownText()));

        // stance
        String stance = argBox.select("div.subtext").iterator().next().text()
                .replaceAll(".*Side: ", "").trim();
        result.setStance(stance);

        StringBuilder sb = new StringBuilder();
        Element argument = argBox.select("div[class=argBody]").first();

        result.setOriginalHTML(argument.html());

        for (Element paragraphElement : argument.select("p")) {
            sb.append(paragraphElement.text());
            sb.append("\n");
        }

        result.setText(Utils.normalize(sb.toString()));

        result.setId(argBox.id());

        return result;
    }

}
