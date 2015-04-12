package de.tudarmstadt.ukp.dkpro.web.comments.createdebate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p/>
 * Parse an html page with JSoup
 * return Main information about the debate
 *
 * @author Anil Narassiguin
 * @author Ivan Habernal
 */

public class CreateDebateHTMLParser
{

    public static Debate parseDebate(InputStream inputStream)
            throws IOException
    {
        return parseDebate(inputStream, null);
    }

    public static Debate parseDebate(InputStream inputStream, String url)
            throws IOException
    {
        Debate result = new Debate();

        Document doc = Jsoup.parse(inputStream, "UTF-8", "http://www.createdebate.com/");

        // Set the Url of the doc
        result.setUrl(doc.select("link").attr("href"));

        // title
        Element body = doc.body();
        String title = body.select("h1[class=debateTitle]").first().ownText();
        result.setTitle(title);

        // sides and scores
        //        DebateSide leftSide = new DebateSide();
        //        leftSide.setSide(DebateSide.Side.LEFT);
        //        DebateSide rightSide = new DebateSide();
        //        rightSide.setSide(DebateSide.Side.RIGHT);

        Element twoSidesAndScores = body
                .select("table[style=margin:0 auto;padding:0;border:0;text-align:center;width:98%;]")
                .first();

        Elements twoSides = twoSidesAndScores.select("div[class=sideTitle]");
        if (twoSides.size() != 2) {
            throw new IllegalArgumentException("Element has not two sides");
        }

        // left side score and stance
        String sideLabelLeft = twoSides.get(0).select("h2[class=sideTitle]").first().ownText();
        String sideScoreLeft = twoSides.get(0).select("span[id~=sideP.*]").first().ownText();

        //        leftSide.setStance(sideLabelLeft);
        //        leftSide.setScore(Integer.parseInt(sideScoreLeft));

        // right side score and stance
        String sideLabelRight = twoSides.get(1).select("h2[class=sideTitle]").first().ownText();
        String sideScoreRight = twoSides.get(1).select("span[id~=sideP.*]").first().ownText();

        //        rightSide.setStance(sideLabelRight);
        //        rightSide.setScore(Integer.parseInt(sideScoreRight));

        // description
        StringBuilder debateDescriptionBuilder = new StringBuilder();
        Element description = body.select("#description").first();

        if (description != null) {
            Element descriptionText = description.select("div[class=centered debatelongDesc]")
                    .first();
            if ("".equals(descriptionText.select("p").first().ownText())) {
                for (Element p : descriptionText.select("p").select("span")) {
                    debateDescriptionBuilder.append(p.ownText());
                    debateDescriptionBuilder.append("\n");
                }
            }

            else {
                for (Element p : descriptionText.select("p")) {
                    debateDescriptionBuilder.append(p.ownText());
                    debateDescriptionBuilder.append("\n");
                }
            }
        }
        result.setDescription(debateDescriptionBuilder.toString().trim());

        Element debateSideBoxL = body.select("div[class=debateSideBox sideL]").first();
        Element debateSideBoxR = body.select("div[class=debateSideBox sideR]").first();

        for (Element argBody : debateSideBoxL.select("div[class=argBox argument][id~=arg]")) {
            Argument argumentWithParent = extractArgument(argBody);
            Element parent = argBody.parent();
            Element previousElement = parent.previousElementSibling();
            String parentId = "no";
            if (previousElement != null) {
                Element realParent = previousElement.previousElementSibling();
                parentId = realParent.id();
            }

            argumentWithParent.setParentId(parentId);
            result.getArgumentList().add(argumentWithParent);
            //            leftSide.add(argumentWithParent);
        }

        for (Element argBody : debateSideBoxR.select("div[class=argBox argument][id~=arg]")) {
            Argument argumentWithParent = extractArgument(argBody);
            Element parent = argBody.parent();
            Element previousElement = parent.previousElementSibling();
            String parentId = null;
            if (previousElement != null) {
                Element realParent = previousElement.previousElementSibling();
                parentId = realParent.id();
            }

            argumentWithParent.setParentId(parentId);

            result.getArgumentList().add(argumentWithParent);
            //            rightSide.add(argumentWithParent);
        }

        //        result.setLeftSide(leftSide);
        //        result.setRightSide(rightSide);

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
        System.out.println(argBox);
        Argument result = new Argument();

        Element name = argBox.select("a[href~=//www.createdebate.com/user/viewprofile/][title]")
                .first();
        result.setAuthor(name.ownText());

        Element argPoints = argBox.select("span[id~=tot.*]").first();
        result.setArgPoints(Integer.parseInt(argPoints.ownText()));

        String stance = null;
        String supEvidence = argBox.select("div[class=supportingEvidence]")
                .select("div[class=subText]").first().ownText();
        Pattern p = Pattern.compile("(?m)(?<=\\bSide: ).*$");
        Matcher m = p.matcher(supEvidence);

        while (m.find()) {
            stance = m.group(0);
        }
        result.setStance(stance);

        StringBuilder sb = new StringBuilder();
        Element argument = argBox.select("div[class=argBody]").first();
        if (argument != null) {
            for (Element paragraphElement : argument.select("p")) {
                sb.append(paragraphElement.ownText());
                sb.append("\n");
            }
        }
        result.setText(sb.toString().trim());
        result.setId(argBox.id());

        return result;
    }

}
