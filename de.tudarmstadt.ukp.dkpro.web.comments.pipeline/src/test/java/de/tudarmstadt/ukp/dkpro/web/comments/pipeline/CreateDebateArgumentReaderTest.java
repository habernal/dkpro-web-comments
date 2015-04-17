package de.tudarmstadt.ukp.dkpro.web.comments.pipeline;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Test;

/**
 * @author Ivan Habernal
 */
public class CreateDebateArgumentReaderTest
{
    @Test
    public void testReadCollection()
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                        CreateDebateArgumentReader.class,
                        CreateDebateArgumentReader.PARAM_SOURCE_LOCATION,
                        "/home/user-ukp/data2/createdebate-exported-2014"
                ),
                AnalysisEngineFactory.createEngineDescription(CharacterFrequencyCounter.class)
        );

    }
}