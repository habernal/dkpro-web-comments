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

package de.tudarmstadt.ukp.dkpro.web.comments.clustering.entropy;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.EmbeddingsSentenceAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

/**
 * @author Ivan Habernal
 */
public class ClusterTopicMatrixMain
{
    private String word2VecFile;

    private String sourceDataDir;

    private String cacheFile;

    private String centroidsFile;

    private String debateTopicFile;

    public void generateClusterTopicMatrix(String outFile)
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory
                        .createReaderDescription(XmiReader.class, XmiReader.PARAM_SOURCE_LOCATION,
                                sourceDataDir, XmiReader.PARAM_PATTERNS,
                                XmiReader.INCLUDE_PREFIX + "*.xmi"),
                AnalysisEngineFactory.createEngineDescription(
                        EmbeddingsSentenceAnnotator.class,
                        EmbeddingsSentenceAnnotator.PARAM_WORD_2_VEC_FILE, word2VecFile,
                        EmbeddingsSentenceAnnotator.PARAM_CACHE_FILE, cacheFile
                ),
                AnalysisEngineFactory.createEngineDescription(
                        ClusterSentencesCollector.class,
                        ClusterSentencesCollector.PARAM_CENTROIDS_FILE, centroidsFile,
                        ClusterSentencesCollector.PARAM_OUTPUT_DIR, "/tmp/out"
                ),
                AnalysisEngineFactory.createEngineDescription(
                        //                        ClusterTopicMatrixGenerator.class,
                        TopNEntriesMatrixGenerator.class,
                        ClusterTopicMatrixGenerator.PARAM_CENTROIDS_FILE, centroidsFile,
                        ClusterTopicMatrixGenerator.PARAM_DEBATE_TOPIC_MAP_FILE, debateTopicFile,
                        ClusterTopicMatrixGenerator.PARAM_OUTPUT_FILE, outFile));
    }

    public static void main(String[] args)
            throws Exception
    {
        ClusterTopicMatrixMain main = new ClusterTopicMatrixMain();
        main.word2VecFile = args[0];
        main.sourceDataDir = args[1];
        main.cacheFile = args[2];
        main.centroidsFile = args[3];
        main.debateTopicFile = args[4];

        // prepare embedding cache
        // write cluto
        main.generateClusterTopicMatrix(args[5]);
        //        main.generateClusterTopicMatrix(args[3]);
    }
}
