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

package xxx.web.comments.pipeline;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author XXX
 */
public class CreateDebateArgumentReaderTest
{
    @Ignore
    @Test
    public void testReadCollection()
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                        DebateArgumentReader.class,
                        DebateArgumentReader.PARAM_SOURCE_LOCATION,
                        "/home/user-ukp/data2/createdebate-exported-2014"
                ),
                AnalysisEngineFactory.createEngineDescription(CharacterFrequencyCounter.class)
        );

    }
}