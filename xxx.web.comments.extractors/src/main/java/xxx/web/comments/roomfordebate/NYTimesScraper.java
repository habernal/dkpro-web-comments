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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import xxx.web.comments.Article;
import xxx.web.comments.Comment;

import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class for scraping data from Room for Debate
 *
 * @author Ivan Habernal
 */
public class NYTimesScraper
{

    /**
     * Crawls all URLs from the given list and stores them in the output folder
     *
     * @param urls      list of urls for Room for debate
     * @param outputDir output
     * @throws IOException ex
     */
    public static void crawlPages(List<String> urls, File outputDir)
            throws IOException
    {
        for (String url : urls) {
            NYTimesCommentsScraper nyTimesCommentsScraper = new NYTimesCommentsScraper();

            String html;
            try {
                html = nyTimesCommentsScraper.readHTML(url);
            }
            catch (InterruptedException e) {
                throw new IOException(e);
            }

            // file name
            File outFile = new File(outputDir, URLEncoder.encode(url, "utf-8") + ".html");

            FileUtils.writeStringToFile(outFile, html);
        }
    }

    /**
     * Extract all comments and stores each comment as preprocessed XMI file (tokenization and
     * sentence splitting) together with a file containing context information (article, previous
     * comments, etc.)
     *
     * @param htmlFile  NYT room for debate html webpage
     * @param outputDir output dir
     * @throws IOException
     * @throws ParseException
     */
    public static void extractDocumentAndComments(File htmlFile, File outputDir)
            throws IOException, ParseException
    {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(htmlFile));

        String html = FileUtils.readFileToString(htmlFile, "utf-8");

        Article article = new NYTimesArticleExtractor().extractArticle(html);
        List<Comment> comments = new NYTimesCommentsScraper().extractComments(html);

        System.out.println(article);
        System.out.println(comments);

        // make an ID-comment map
        Map<String, Comment> idCommentMap = new HashMap<>();
        for (Comment comment : comments) {
            idCommentMap.put(comment.getId(), comment);
        }

        IOUtils.closeQuietly(inputStream);

        // we want to keep the context of each comment
        for (Comment comment : comments) {

            // it's a reaction to another comment
            Comment parentComment = (comment.getParentId() != null ?
                    idCommentMap.get(comment.getParentId()) :
                    null);

            // it' a both reaction to other comment and there are some preceding comments
            Comment previousComment = (comment.getPreviousPostId() != null ?
                    idCommentMap.get(comment.getPreviousPostId()) : null);

            // lets do some UIMA preprocessing
            try {
                // create JCas
                JCas jCas = JCasFactory.createJCas();
                jCas.setDocumentLanguage("en");
                jCas.setDocumentText(comment.getText());
                // metadata
                DocumentMetaData metaData = DocumentMetaData.create(jCas);
                metaData.setDocumentId(comment.getId());
                // we set the the id as title
                metaData.setDocumentTitle(comment.getId());

                // pipeline
                SimplePipeline.runPipeline(jCas,
                        AnalysisEngineFactory.createEngineDescription(ParagraphSplitter.class),
                        AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class),
                        AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
                                XmiWriter.PARAM_TARGET_LOCATION, outputDir)
                );
            }
            catch (UIMAException e) {
                throw new IOException(e);
            }

            // and now save the context in HTML file
            saveCommentContext(comment.getId(), article, parentComment, previousComment,
                    new File(outputDir, comment.getId() + "_context.html"));

        }

        System.out.println("-------");
    }

    /**
     * Saves the context of the comment (article, parent comment, previous comment) into a
     * html file with the given id
     *
     * @param id              id
     * @param article         article
     * @param parentComment   parent comment (can be null)
     * @param previousComment previous comment (can be null)
     * @param file            output file
     * @throws IOException exception
     */
    private static void saveCommentContext(String id, Article article, Comment parentComment,
            Comment previousComment, File file)
            throws IOException
    {
        PrintWriter pw = new PrintWriter(new FileOutputStream(file));
        pw.println("  <?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "  <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
                + "    \"http://www.w3.org/TR/xhtml1/DTD/strict.dtd\">\n"
                + "  <html xmlns=\"http://www.w3.org/TR/xhtml1/strict\" >\n"
                + "    <head>\n"
                + "      <title>" + id + "</title>\n"
                + "    </head>\n"
                + "    <body>\n");

        pw.println("<h1>" + id + " context</h1>");
        pw.println("<h2>Article: " + article.getTitle() + "</h2>");
        for (String paragraph : article.getText().split("\n")) {
            pw.println("<p>" + paragraph + "</p>");
        }

        if (parentComment != null) {

            pw.println("<h3>Parent comment</h3>");
            for (String paragraph : parentComment.getText().split("\n")) {
                pw.println("<p>" + paragraph + "</p>");
            }
        }

        if (previousComment != null) {
            pw.println("<h3>Previous comment</h3>");
            for (String paragraph : previousComment.getText().split("\n")) {
                pw.println("<p>" + paragraph + "</p>");
            }
        }

        pw.println("</body></html>");
        pw.close();
    }

    public static void main(String[] args)
            throws Exception
    {
        String crawledPagesFolder = args[0];
        String outputFolder = args[1];

        // read links from text file
        InputStream urlsStream = NYTimesScraper.class.getClassLoader()
                .getResourceAsStream("urls.txt");
        List<String> urls = IOUtils.readLines(urlsStream);

        // download all
        //        crawlPages(urls, new File(crawledPagesFolder));

        Collection<File> files = FileUtils
                .listFiles(new File(crawledPagesFolder), null, false);

        System.out.println(files);

        for (File file : files) {
            extractDocumentAndComments(file, new File(outputFolder));
        }
    }
}
