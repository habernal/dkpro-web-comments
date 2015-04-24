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

package de.tudarmstadt.ukp.dkpro.web.comments.pipeline.tmp;

import cc.mallet.topics.ParallelTopicModel;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTokenizer;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel.MalletTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.web.comments.pipeline.CreateDebateArgumentReader;
import de.tudarmstadt.ukp.dkpro.web.comments.pipeline.VocabularyCollector;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.File;

/**
 * (c) 2015 Ivan Habernal
 */
public class TopicModelGenerator
{
    private static final int N_THREADS = 2;

    /**
     * Collects and stores vocabulary
     *
     * @param corpusPath     corpus
     * @param vocabularyFile vocabulary
     * @throws Exception
     */
    public static void collectVocabulary(String corpusPath, String vocabularyFile)
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                        XmiReader.class,
                        XmiReader.PARAM_SOURCE_LOCATION, corpusPath,
                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"),

                AnalysisEngineFactory.createEngineDescription(VocabularyCollector.class,
                        VocabularyCollector.PARAM_MINIMAL_OCCURRENCE, 5,
                        VocabularyCollector.PARAM_IGNORE_STOPWORDS, true,
                        VocabularyCollector.PARAM_MODEL_LOCATION, vocabularyFile)
        );

        /*
        INFORMATION: Original vocabulary size: 130853
        INFORMATION: Filtered vocabulary size: 30419
        total tokens: 11,393,646
        */
    }

    public static void trainTopicModel(String corpusPath, String vocabularyFile,
            String topicModelFile)
            throws Exception
    {
        int nTopics = 100;
        int nIterations = 100;

        SimplePipeline.runPipeline(
                // reader
                CollectionReaderFactory.createReaderDescription(
                        XmiReader.class,
                        XmiReader.PARAM_SOURCE_LOCATION, corpusPath,
                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"),

                // tokenize web-texts
                AnalysisEngineFactory.createEngineDescription(ArktweetTokenizer.class),

                // lemmatizer
                AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class),

                AnalysisEngineFactory.createEngineDescription(
                        ExtendedMalletTopicModelEstimator.class,
                        ExtendedMalletTopicModelEstimator.PARAM_N_THREADS, N_THREADS,
                        ExtendedMalletTopicModelEstimator.PARAM_TARGET_LOCATION, topicModelFile,
                        ExtendedMalletTopicModelEstimator.PARAM_N_ITERATIONS, nIterations,
                        ExtendedMalletTopicModelEstimator.PARAM_N_TOPICS, nTopics,
                        ExtendedMalletTopicModelEstimator.PARAM_USE_LEMMA, true,
                        ExtendedMalletTopicModelEstimator.PARAM_VOCABULARY_FILE, vocabularyFile
                )
        );

        ParallelTopicModel model = ParallelTopicModel.read(new File(topicModelFile));
        System.out.println(model.getNumTopics());
    }

    public static void main(String[] args)
            throws Exception
    {
        String corpusPath = args[0];
        String vocabularyFile = args[1];
        String topicModelFile = args[2];

        collectVocabulary(corpusPath, vocabularyFile);
        trainTopicModel(corpusPath, vocabularyFile, topicModelFile);
        //        testEstimatorSentence();
    }

    public static void testEstimatorSentence(String corpusPath, String modelFile)
            throws Exception
    {
        int nTopics = 10;
        int nIterations = 50;
        boolean useLemmas = false;
        String language = "en";
        String entity = Sentence.class.getName();

        SimplePipeline.runPipeline(
                // reader
                CollectionReaderFactory.createReaderDescription(
                        CreateDebateArgumentReader.class,
                        CreateDebateArgumentReader.PARAM_SOURCE_LOCATION,
                        corpusPath),

                // tokenize web-texts
                AnalysisEngineFactory.createEngineDescription(ArktweetTokenizer.class),

                // lemmatizer
                AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class),

                // sentence splitter
                AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class,
                        StanfordSegmenter.PARAM_WRITE_TOKEN, false),

                AnalysisEngineFactory.createEngineDescription(
                        MalletTopicModelInferencer.class,
                        MalletTopicModelInferencer.PARAM_USE_LEMMA, true,
                        MalletTopicModelInferencer.PARAM_MODEL_LOCATION, modelFile,
                        MalletTopicModelInferencer.PARAM_TYPE_NAME,
                        Sentence.class.getCanonicalName()),

                AnalysisEngineFactory.createEngineDescription(
                        CasDumpWriter.class
                )
        );

        //        assertTrue(MODEL_FILE.exists());
        //        ParallelTopicModel model = ParallelTopicModel.read(MODEL_FILE);
        //        assertEquals(nTopics, model.getNumTopics());
    }

    /*
    @Test
    public void testEstimatorAlphaBeta()
        throws Exception
    {
        int nTopics = 10;
        int nIterations = 50;
        float alpha = nTopics / 50.0f;
        float beta = 0.01f;
        String language = "en";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription estimator = createEngineDescription(
                MalletTopicModelEstimator.class,
                MalletTopicModelEstimator.PARAM_N_THREADS, N_THREADS,
                MalletTopicModelEstimator.PARAM_TARGET_LOCATION, MODEL_FILE,
                MalletTopicModelEstimator.PARAM_N_ITERATIONS, nIterations,
                MalletTopicModelEstimator.PARAM_N_TOPICS, nTopics,
                MalletTopicModelEstimator.PARAM_ALPHA_SUM, alpha,
                MalletTopicModelEstimator.PARAM_BETA, beta);
        SimplePipeline.runPipeline(reader, segmenter, estimator);

        assertTrue(MODEL_FILE.exists());
        ParallelTopicModel model = ParallelTopicModel.read(MODEL_FILE);
        assertEquals(nTopics, model.getNumTopics());
    }

     */

}
