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

package de.tudarmstadt.ukp.dkpro.web.comments.roomfordebate;

import de.tudarmstadt.ukp.dkpro.web.comments.Article;
import de.tudarmstadt.ukp.dkpro.web.comments.roomfordebate.NYTimesArticleExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * (c) 2015 Ivan Habernal
 */
public class NYTimesArticleExtractorTest
{

    @Test public void testReadHTML()
            throws Exception
    {
        NYTimesArticleExtractor extractor = new NYTimesArticleExtractor();

        String html = extractor.readHTML(
                "http://www.nytimes.com/roomfordebate/2015/02/04/regulate-internet-providers/the-internet-is-back-to-solid-regulatory-ground");

        System.out.println(html);

    }

    @Test public void textExtractArticle()
            throws Exception
    {
        NYTimesArticleExtractor extractor = new NYTimesArticleExtractor();

        StringWriter writer = new StringWriter();
        IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("nytimes-step1.html"),
                writer, "utf-8");
        String html = writer.toString();

        System.out.println(extractor.extractArticle(html));
    }

    @Ignore
    @Test public void testAll()
            throws Exception
    {

        File folder = new File("/tmp/nytimes-exported2");

        List<Article> articleList = new ArrayList<Article>();

        for (File f : folder.listFiles()) {
            NYTimesArticleExtractor extractor = new NYTimesArticleExtractor();

            try {
                Article a = extractor.extractArticle(FileUtils.readFileToString(f));
                articleList.add(a);
            }
            catch (Exception ex) {
                System.err.println("Failed on " + f.getAbsolutePath());
                //				ex.printStackTrace();
            }
        }

        System.out.println(articleList.size());
    }
}