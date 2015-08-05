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

package xxx.web.comments.debates.impl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolSegmenter;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ivan Habernal
 */
public class ProConOrgCommentsParser
        implements DebateParser
{
    // for counting ids
    private int idCounter = 0;

    @Override
    public Debate parseDebate(InputStream inputStream)
            throws IOException
    {
        Debate result = new Debate();

        Document doc = Jsoup.parse(inputStream, "UTF-8", "http://www.procon.org/");

        // Set the Url of the doc

        // title
        Element body = doc.body();

        Map<String, Elements> proConElements = new HashMap<>();
        proConElements.put("pro", body.select("div[class=column pro]"));
        proConElements.put("con", body.select("div[class=column con]"));

        //        Elements pro = body.select("div[class=column pro]");
        //        System.out.println(pro);

        // title
        result.setTitle(Utils.normalize(body.select("h2").text()));

        for (Map.Entry<String, Elements> entry : proConElements.entrySet()) {
            // stance
            String stance = entry.getKey();

            Elements comments = entry.getValue().select("ul.comments > li[class^=comment]");

            for (Element element : comments) {

                Element divContent = element.select("div.contents").iterator().next();

                // extract argument content
                Argument argument = extractArgumentFromDivContent(divContent);
                // extract ID
                String parentId = element.attr("id").replace(":", "_");

                if (parentId == null) {
                    throw new IllegalStateException("Parent id must be known");
                }

                argument.setId(parentId);
                // set stance - we know it
                argument.setStance(stance);

                result.getArgumentList().add(argument);

                Elements divReplies = element.select("li[class^=reply]");

                //                System.out.println(divReplies.size());

                for (Element divReply : divReplies) {

                    Element replyDivContent = divReply.select("div.contents").iterator().next();

                    // extract reply argument
                    Argument replyArgument = extractArgumentFromDivContent(replyDivContent);

                    // set id and parentId
                    String id = element.attr("id").replace(":", "_");
                    replyArgument.setId(id);

                    if (id == null) {
                        throw new IllegalStateException("Id must be known");
                    }

                    replyArgument.setParentId(parentId);

                    // add to debate
                    result.getArgumentList().add(replyArgument);
                }

            }
        }

        return result;
    }

    protected static Argument extractArgumentFromDivContent(Element divContent)
    {
        Argument argument = new Argument();

        Element blockquote = divContent.select("blockquote").iterator().next();
        //        System.out.println("----------");

        String text = ProConOrgParser.extractPlainTextFromTextElement(blockquote);
        argument.setText(text);

        String votesUpText = divContent.select("span.votes-up").text();
        String votesDownText = divContent.select("span.votes-down").text();

        int votesUp = Integer.valueOf(votesUpText);
        int votesDown = Integer.valueOf(votesDownText);

        argument.setVoteUpCount(votesUp);
        argument.setVoteDownCount(Math.abs(votesDown));

        argument.setAuthor(divContent.select("span.name").text());

        // time
        DateFormat df = new SimpleDateFormat("MMM. dd, yyyy", Locale.ENGLISH);
        String dateText = divContent.select("span.date").text();
        try {
            Date date = df.parse(dateText);
            argument.setTimestamp(date);
        }
        catch (ParseException e) {
            // e.printStackTrace();
        }

        //        System.out.println(argument);

        return argument;
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

    public static void main(String[] args)
            throws Exception
    {
        File inFolder = new File(args[0]);
        File outFolder = new File(args[1]);

        File[] files = inFolder.listFiles();
        if (files == null) {
            throw new IOException("No such dir: " + inFolder);
        }

        DebateParser debateParser = new ProConOrgCommentsParser();

        for (File f : files) {
            InputStream inputStream = new FileInputStream(f);
            Debate debate;
            try {
                debate = debateParser.parseDebate(inputStream);
                if (debate == null) {
                    //                    throw new IllegalArgumentException(f.getAbsolutePath());
                    System.err.println(f.getAbsolutePath() + " debate null");
                }
            }
            catch (Exception ex) {
                throw new Exception(f.getAbsolutePath(), ex);
            }
            IOUtils.closeQuietly(inputStream);

            if (debate != null) {

                //            System.out.println(debate);
                for (Argument argument : debate.getArgumentList()) {
                    String title = debate.getTitle().replaceAll("[^A-Za-z0-9]", "_");

                    String docId = argument.getId() + "_" + argument.getStance() + "_" + title;

                    // lets do some UIMA preprocessing
                    try {
                        // create JCas
                        JCas jCas = JCasFactory.createJCas();
                        jCas.setDocumentLanguage("en");
                        jCas.setDocumentText(argument.getText());

                        // metadata
                        DocumentMetaData metaData = DocumentMetaData.create(jCas);
                        metaData.setDocumentId(docId);
                        // we set the the id as title
                        metaData.setDocumentTitle(docId);

                        // pipeline
                        SimplePipeline.runPipeline(jCas,
                                AnalysisEngineFactory
                                        .createEngineDescription(ParagraphSplitter.class),
                                // only this segmenter can deal with multiple dots etc. - use this one!
                                AnalysisEngineFactory
                                        .createEngineDescription(LanguageToolSegmenter.class),
                                AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
                                        XmiWriter.PARAM_TARGET_LOCATION, outFolder)
                        );

                        for (Sentence s : JCasUtil.select(jCas, Sentence.class)) {
                            System.out.println(s.getCoveredText());
                        }
                    }
                    catch (UIMAException e) {
                        throw new IOException(
                                f.getAbsolutePath() + "\n" + argument.getOriginalHTML(), e);
                    }
                }

            }
        }

    }
}
