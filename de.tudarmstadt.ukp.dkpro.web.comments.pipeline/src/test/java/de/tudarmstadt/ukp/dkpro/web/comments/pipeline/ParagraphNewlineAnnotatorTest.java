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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Ivan Habernal
 */
@RunWith(value = Parameterized.class)
public class ParagraphNewlineAnnotatorTest
{
    private String text;
    private int paragraphs;

    public ParagraphNewlineAnnotatorTest(String text, int paragraphs)
    {
        this.text = text;
        this.paragraphs = paragraphs;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData()
    {
        return Arrays.asList(new Object[][] {
                { "one\ntwo\nthree", 3 },
                { "one paragraph", 1 },
                { "\nempty paragraphs\n", 1 },
                { "\n\nempty paragraphs\n\n\n", 1 },
        });
    }

    @Test
    public void testAnnotate()
            throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText(this.text);
        jcas.setDocumentLanguage("en");

        SimplePipeline.runPipeline(jcas,
                AnalysisEngineFactory.createEngineDescription(ParagraphNewlineAnnotator.class)
        );

        Collection<Paragraph> paragraphs = JCasUtil.select(jcas, Paragraph.class);

        assertEquals(this.paragraphs, paragraphs.size());
    }
}