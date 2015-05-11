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

package de.tudarmstadt.ukp.dkpro.web.comments.clustering;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.embeddings.EmbeddingsSentenceAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.File;

/**
 * (c) 2015 Ivan Habernal
 */
public class ClutoMain
{

    private String word2VecFile;

    private String sourceDataDir;

    private String cacheFile;

    private String clutoMatrixFile;

    public void prepareEmbeddingCache()
            throws Exception
    {
        if (!(new File(cacheFile).exists())) {
            SimplePipeline.runPipeline(CollectionReaderFactory
                    .createReaderDescription(XmiReader.class, XmiReader.PARAM_SOURCE_LOCATION,
                            sourceDataDir, XmiReader.PARAM_PATTERNS,
                            XmiReader.INCLUDE_PREFIX + "*.xmi"), AnalysisEngineFactory
                    .createEngineDescription(EmbeddingsCachePreprocessor.class,
                            EmbeddingsCachePreprocessor.PARAM_WORD_2_VEC_FILE, word2VecFile,
                            EmbeddingsCachePreprocessor.PARAM_CACHE_FILE, cacheFile));
        }
    }

    public void generateClutoMatrix()
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
                        EmbeddingsClutoDataWriter.class,
                        EmbeddingsClutoDataWriter.PARAM_OUTPUT_FOLDER, clutoMatrixFile));
    }

    public static void main(String[] args)
            throws Exception
    {
        ClutoMain main = new ClutoMain();
        main.word2VecFile = args[0];
        main.sourceDataDir = args[1];
        main.cacheFile = args[2];
        main.clutoMatrixFile = args[3];

        // prepare embedding cache
        main.prepareEmbeddingCache();

        // write cluto
        main.generateClutoMatrix();
    }
}
