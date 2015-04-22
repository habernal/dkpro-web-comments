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

package de.tudarmstadt.ukp.dkpro.web.comments.pipeline;

import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTokenizer;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

/**
 * @author Ivan Habernal
 */
public class DebatesToXMIPipeline
{
    public static void main(String[] args)
    {
        String inFolder = args[0];
        String outFolder = args[1];
        try {
            SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                            CreateDebateArgumentReader.class,
                            CreateDebateArgumentReader.PARAM_SOURCE_LOCATION,
                            inFolder),

                    // paragraphs
                    AnalysisEngineFactory.createEngineDescription(ParagraphSplitter.class),
                    // tokenize web-texts
                    AnalysisEngineFactory.createEngineDescription(ArktweetTokenizer.class),
                    // find sentences
                    AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class,
                            StanfordSegmenter.PARAM_WRITE_TOKEN, false),
                    // lemma
                    AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class),

                    AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
                            XmiWriter.PARAM_TARGET_LOCATION, outFolder)
            );
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}