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

package xxx.web.comments.clustering.topic;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

/**
 * Assigns topic distribution to the entire document
 *
 * @author XXX
 */
public class DocumentTopicAnnotator
        extends AbstractTopicAnnotator
{
    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        annotateWithTopicDistribution(aJCas, 0, aJCas.getDocumentText().length());
    }
}
