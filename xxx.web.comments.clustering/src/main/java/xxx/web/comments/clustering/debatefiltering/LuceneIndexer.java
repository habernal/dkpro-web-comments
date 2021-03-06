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

package xxx.web.comments.clustering.debatefiltering;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import xxx.web.comments.pipeline.FullDebateContentReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;

/**
 * (c) 2015 XXX
 */
public class LuceneIndexer
        extends JCasConsumer_ImplBase
{

    public static final String PARAM_LUCENE_INDEX_DIR = "luceneIndexDir";
    @ConfigurationParameter(name = PARAM_LUCENE_INDEX_DIR)
    File luceneIndexDir;

    public static final String FIELD_TEXT_CONTENT = "textContent";
    public static final String FIELD_FILE = "file";

    private IndexWriter iwriter;
    private Directory directory;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
            directory = FSDirectory.open(luceneIndexDir);

            iwriter = new IndexWriter(directory, analyzer, true,
                    new IndexWriter.MaxFieldLength(50000));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        Document doc = new Document();

        // index text
        doc.add(new Field(FIELD_TEXT_CONTENT, aJCas.getDocumentText(), Field.Store.YES,
                Field.Index.ANALYZED));

        // localize input dir
        String url = DocumentMetaData.get(aJCas).getDocumentId();
        String fileName = url.replaceAll("http://", "").replaceAll("/", "___") + ".xml";

        doc.add(new Field(FIELD_FILE, fileName, Field.Store.YES, Field.Index.NOT_ANALYZED));

        try {
            iwriter.addDocument(doc);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            iwriter.close();
            directory.close();
        }
        catch (IOException ex) {
            throw new AnalysisEngineProcessException(ex);
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory
                        .createReaderDescription(FullDebateContentReader.class,
                                FullDebateContentReader.PARAM_SOURCE_LOCATION,
                                "/home/habi/research/data/debates-xml/"),
                AnalysisEngineFactory.createEngineDescription(LuceneIndexer.class,
                        LuceneIndexer.PARAM_LUCENE_INDEX_DIR, "/tmp/lucene"));


    }
}
