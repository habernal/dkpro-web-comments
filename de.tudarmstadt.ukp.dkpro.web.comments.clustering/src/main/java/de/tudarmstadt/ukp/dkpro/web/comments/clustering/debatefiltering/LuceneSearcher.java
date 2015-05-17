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

package de.tudarmstadt.ukp.dkpro.web.comments.clustering.debatefiltering;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * (c) 2015 Ivan Habernal
 */
public class LuceneSearcher
{

    private File luceneIndexDir;

    public static void main(String[] args) throws Exception
    {
        LuceneSearcher luceneSearcher = new LuceneSearcher();
        luceneSearcher.luceneIndexDir = new File("/tmp/lucene");

        List<String> homeschooling = luceneSearcher.retrieveTopNDocs("homeschooling", 500);
        System.out.println(homeschooling);
    }

    public List<String> retrieveTopNDocs(String textQuery, int topN) throws Exception {
        // Now search the index:
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

        Directory directory = FSDirectory.open(luceneIndexDir);
        IndexSearcher indexSearcher = new IndexSearcher(directory, true);

        // Parse a simple query
        QueryParser parser = new QueryParser(Version.LUCENE_30, LuceneIndexer.FIELD_TEXT_CONTENT, analyzer);
        Query query = parser.parse(textQuery);

        ScoreDoc[] hits = indexSearcher.search(query, null, topN).scoreDocs;

        List<String> result = new ArrayList<>();

        // Iterate through the results:
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = indexSearcher.doc(hits[i].doc);
            result.add(hitDoc.getField(LuceneIndexer.FIELD_FILE).stringValue());
//            System.out.println(hitDoc.toString());
            //                assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
        }
        indexSearcher.close();
        directory.close();

        return result;
    }
}
