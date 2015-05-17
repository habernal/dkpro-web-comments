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

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.topic.DocumentTopicAnnotator;
import de.tudarmstadt.ukp.dkpro.web.comments.pipeline.ArktweetTokenizerFixed;
import de.tudarmstadt.ukp.dkpro.web.comments.pipeline.SentenceOverlapSanityCheck;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

/**
 * (c) 2015 Ivan Habernal
 */
public class DebateFilterByTopicSimilarity
{

    private String unlabeledDocumentsDir;
    private String topicModel;
    private String outputDomainTopicVector;

    public void createDomainTopicVectors()
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory
                        .createReaderDescription(XmiReader.class, XmiReader.PARAM_SOURCE_LOCATION,
                                this.unlabeledDocumentsDir, XmiReader.PARAM_PATTERNS,
                                XmiReader.INCLUDE_PREFIX + "*.xmi"),

                // paragraphs
                AnalysisEngineFactory.createEngineDescription(ParagraphSplitter.class),
                // tokenize web-texts
                AnalysisEngineFactory.createEngineDescription(ArktweetTokenizerFixed.class),
                // find sentences
                AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class,
                        StanfordSegmenter.PARAM_WRITE_TOKEN, false),
                // lemma
                AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class),
                // sanity check
                AnalysisEngineFactory.createEngineDescription(SentenceOverlapSanityCheck.class),

                AnalysisEngineFactory.createEngineDescription(DocumentTopicAnnotator.class,
                        DocumentTopicAnnotator.PARAM_MODEL_LOCATION, this.topicModel),
                AnalysisEngineFactory.createEngineDescription(DomainTopicVectorProducer.class,
                        DomainTopicVectorProducer.PARAM_OUTPUT_FILE, this.outputDomainTopicVector));
    }

    public static void main(String[] args)
            throws Exception
    {
        DebateFilterByTopicSimilarity filter = new DebateFilterByTopicSimilarity();

        // unlabeled documents
        filter.unlabeledDocumentsDir = args[0];
        // existing topic model
        filter.topicModel = args[1];
        // output map domain/avg.topic vector
        filter.outputDomainTopicVector = args[2];

        /*
        Two documents are empty: 4636, 4657 (grep "sofaString=\"\"" *) - delete them manually
        in advance
         */

        // create domain/topic vectors
        filter.createDomainTopicVectors();
    }
}
