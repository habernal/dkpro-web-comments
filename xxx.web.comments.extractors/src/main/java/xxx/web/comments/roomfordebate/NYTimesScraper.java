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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import xxx.web.comments.Article;
import xxx.web.comments.Comment;

import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

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

    public static void extractDocumentAndComments(File htmlFile, File outputDir)
            throws IOException, ParseException
    {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(htmlFile));

        String html = FileUtils.readFileToString(htmlFile, "utf-8");

        Article article = new NYTimesArticleExtractor().extractArticle(html);
        List<Comment> comments = new NYTimesCommentsScraper().extractComments(html);

        System.out.println(article);
        System.out.println(comments);

        IOUtils.closeQuietly(inputStream);
    }

    public static void main(String[] args)
            throws Exception
    {
        String crawledPagesFolder = args[0];

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
            extractDocumentAndComments(file, null);
        }

        //
        //        NYTimesCommentsScraper nyTimesCommentsScraper = new NYTimesCommentsScraper();
        //            System.out.println(link);
        //
        //            String html = nyTimesCommentsScraper.readHTML(link);
        //            //                    "http://www.nytimes.com/roomfordebate/2015/06/30/should-greece-abandon-the-euro/the-euro-is-a-straitjacket-for-greece");
        //            //                File tmpFile = new File("src/test/resources/nytimes-step2.html");
        //            //            FileUtils.writeStringToFile(tmpFile, html);
        //
        //            List list = new ArrayList();
        //
        //            list.add(article);
        //
        //            List comments = nyTimesCommentsScraper
        //                    .extractComments(IOUtils.toInputStream(html, "utf-8"));
        //
        //            list.addAll(comments);
        //
        //            //                for (Comment comment : comments) {
        //            //                    System.out.println(comment);
        //            //                }
        //
        //            FileOutputStream fos = new FileOutputStream(File.createTempFile("nyt", ".xml"));
        //            XStream xStream = new XStream();
        //
        //            xStream.toXML(list, fos);
        //
        //            fos.close();
        //        }

    }
}
