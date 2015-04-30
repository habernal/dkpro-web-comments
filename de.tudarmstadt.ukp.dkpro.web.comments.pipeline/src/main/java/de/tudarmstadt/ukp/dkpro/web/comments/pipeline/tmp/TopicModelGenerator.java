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
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import de.tudarmstadt.ukp.dkpro.web.comments.pipeline.ArktweetTokenizerFixed;
import de.tudarmstadt.ukp.dkpro.web.comments.pipeline.FullDebateContentReader;
import de.tudarmstadt.ukp.dkpro.web.comments.pipeline.VocabularyCollector;
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

    public static void trainTopicModelOnDebates(String corpusPath, String vocabularyFile,
            String topicModelFile)
            throws Exception
    {
        int nTopics = 100;
        int nIterations = 100;

        SimplePipeline.runPipeline(
                // reader
                CollectionReaderFactory.createReaderDescription(
                        FullDebateContentReader.class,
                        FullDebateContentReader.PARAM_SOURCE_LOCATION, corpusPath),
                // tokenize web-texts
                AnalysisEngineFactory.createEngineDescription(ArktweetTokenizerFixed.class),
                // find sentences
                AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class,
                        StanfordSegmenter.PARAM_WRITE_TOKEN, false),
                // lemma
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

    public static void trainTopicModelOnArguments(String corpusPath, String vocabularyFile,
            String topicModelFile)
            throws Exception
    {
        int nTopics = 50;
        int nIterations = 100;

        SimplePipeline.runPipeline(
                // reader
                CollectionReaderFactory.createReaderDescription(
                        XmiReader.class,
                        XmiReader.PARAM_SOURCE_LOCATION, corpusPath,
                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"),

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

        //        collectVocabulary(corpusPath, vocabularyFile);
//        trainTopicModelOnArguments(corpusPath, vocabularyFile, topicModelFile);
        trainTopicModelOnDebates(corpusPath, vocabularyFile, topicModelFile);
    }
}
