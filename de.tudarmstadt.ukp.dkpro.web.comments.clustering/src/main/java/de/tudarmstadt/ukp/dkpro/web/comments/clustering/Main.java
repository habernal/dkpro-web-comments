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

import de.tudarmstadt.ukp.dkpro.web.comments.pipeline.CreateDebateArgumentReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.File;

/**
 * (c) 2015 Ivan Habernal
 */
public class Main
{

    public static final String EMBEDDINGS_CACHE_BIN = "/tmp/embeddings-cache.bin";

    public static void prepareEmbeddingCache()
            throws Exception
    {
        if (!(new File(EMBEDDINGS_CACHE_BIN).exists())) {
            SimplePipeline.runPipeline(CollectionReaderFactory
                            .createReaderDescription(CreateDebateArgumentReader.class,
                                    CreateDebateArgumentReader.PARAM_SOURCE_LOCATION,
                                    "/home/user-ukp/data2/createdebate-exported-2014"),
                    AnalysisEngineFactory.createEngineDescription(EmbeddingsCachePreprocessor.class,
                            EmbeddingsCachePreprocessor.PARAM_CACHE_FILE, EMBEDDINGS_CACHE_BIN));
        }
    }

    public static void generateClutoMatrix()
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory
                        .createReaderDescription(CreateDebateArgumentReader.class,
                                CreateDebateArgumentReader.PARAM_SOURCE_LOCATION,
                                "/home/user-ukp/data2/createdebate-exported-2014"),
                AnalysisEngineFactory.createEngineDescription(EmbeddingsClutoDataWriter.class,
                        EmbeddingsClutoDataWriter.PARAM_CACHE_FILE, EMBEDDINGS_CACHE_BIN,
                        EmbeddingsClutoDataWriter.PARAM_OUTPUT_FOLDER, "/tmp/cluto.txt"));
    }

    public static void main(String[] args)
            throws Exception
    {
        // prepare embedding cache
        prepareEmbeddingCache();

        // write cluto
        generateClutoMatrix();
    }
}
