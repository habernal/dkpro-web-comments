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
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
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

        Elements pro = body.select("div[class=column pro]");
//        System.out.println(pro);

        Elements comments = pro.select("ul[class=comments] > li[class^=comment]");
        System.out.println(comments.size());

        for (Element element : comments) {
            Element blockquote = element.select("blockquote").iterator().next();
            System.out.println("----------");

            System.out.println(ProConOrgParser.extractPlainTextFromTextElement(blockquote));
        }

        //TODO: finish

        //        Elements debateTitleElements = body.select("h2");
        //        Elements debateTitleElements = body.select("p[class=title]").select("p[style]");

//        if (debateTitleElements.first() == null) {
            // not a debate
//            return null;
//        }

//        String title = Utils.normalize(debateTitleElements.first().text());
//        result.setTitle(title);



        /*
        Elements proConTr = body.select("tr > td > b:contains(PRO \\(yes\\))");

        if (proConTr == null || proConTr.parents() == null ||
                proConTr.parents().first() == null ||
                proConTr.parents().first().parents() == null ||
                proConTr.parents().first().parents().first() == null ||
                proConTr.parents().first().parents().first().nextElementSibling() == null) {
            // not a pro-con debate
            return null;
        }

        Element trAnswers = proConTr.parents().first()
                .parents().first().nextElementSibling();

        // the PRO side
        Element proTd = trAnswers.select("td").get(0);
        Element conTd = trAnswers.select("td").get(1);

        //        System.out.println(proTd.select("blockquote").size());
        //        System.out.println(conTd.select("blockquote").size());

        for (Element text : proTd.select("blockquote > div[class=editortext]")) {
            Argument argument = new Argument();
            argument.setStance("pro");
            argument.setText(extractPlainTextFromTextElement(text));
            argument.setOriginalHTML(text.html());

            // set ID
            idCounter++;
            argument.setId("pcq_" + idCounter);

            if (!argument.getText().isEmpty()) {
                result.getArgumentList().add(argument);
            }
            else {
                System.err.println("Failed to extract text from " + text.html());
            }
        }

        for (Element text : conTd.select("blockquote > div[class=editortext]")) {
            Argument argument = new Argument();
            argument.setStance("con");
            argument.setText(extractPlainTextFromTextElement(text));
            argument.setOriginalHTML(text.html());

            idCounter++;
            argument.setId("pcq_" + idCounter);

            if (!argument.getText().isEmpty()) {
                result.getArgumentList().add(argument);
            }
            else {
                System.err.println("Failed to extract text from " + text.html());
            }
        }

        // show some stats:
        Map<String, Integer> map = new HashMap<>();
        map.put("pro", 0);
        map.put("con", 0);
        for (Argument argument : result.getArgumentList()) {
            map.put(argument.getStance(), map.get(argument.getStance()) + 1);
        }
        System.out.println(map);

*/
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

                        System.out.println(docId);

                        // pipeline
                        SimplePipeline.runPipeline(jCas,
                                AnalysisEngineFactory
                                        .createEngineDescription(ParagraphSplitter.class),
                                AnalysisEngineFactory
                                        .createEngineDescription(StanfordSegmenter.class),
                                AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
                                        XmiWriter.PARAM_TARGET_LOCATION, outFolder)
                        );
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
