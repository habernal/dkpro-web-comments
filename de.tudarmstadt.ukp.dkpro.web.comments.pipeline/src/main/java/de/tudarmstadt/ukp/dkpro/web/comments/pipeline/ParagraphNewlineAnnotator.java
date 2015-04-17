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
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Annotates paragraphs in the text based on occurrences of newline char "\n"
 * (see {@link de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph}).
 * Ignores empty paragraphs (with zero length).
 *
 * @author Ivan Habernal
 */
public class ParagraphNewlineAnnotator
        extends JCasAnnotator_ImplBase
{

    public static final String NEWLINE_CHARACTER = "\n";

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        String documentText = aJCas.getDocumentText();
        List<Integer> newLineIndices = findAllNewLineIndices(documentText);

        Iterator<Integer> iterator = newLineIndices.iterator();
        int previousIndex = 0;
        while (iterator.hasNext()) {
            Integer currentIndex = iterator.next();
            // label paragraph
            // but ignore empty paragraphs (with zero length)
            if (currentIndex > previousIndex) {
                Paragraph p = new Paragraph(aJCas, previousIndex, currentIndex);
                p.addToIndexes();
            }

            previousIndex = currentIndex + 1;
        }

        // last paragraph (again, no empty paragraphs at the end of the document)
        if (documentText.length() > previousIndex) {
            Paragraph p = new Paragraph(aJCas, previousIndex, documentText.length());
            p.addToIndexes();
        }
    }

    /**
     * Returns a list of all indices of "\n" character in string
     *
     * @param s string
     * @return list (may be empty, never null)
     */
    static List<Integer> findAllNewLineIndices(String s)
    {
        List<Integer> result = new ArrayList<>();
        int i = -1;
        while ((i = s.indexOf(NEWLINE_CHARACTER, i + 1)) != -1) {
            result.add(i);
        }

        return result;
    }

    public static void main(String[] args)
            throws UIMAException
    {
        String text = "01" + NEWLINE_CHARACTER + "34" + NEWLINE_CHARACTER + "67";

        for (String s : Arrays.asList(text, "one paragraph", "\nempty paragraphs\n")) {
            System.out.println(findAllNewLineIndices(text));
            JCas jcas = JCasFactory.createJCas();
            jcas.setDocumentText(s);
            jcas.setDocumentLanguage("en");

            SimplePipeline.runPipeline(jcas,
                    AnalysisEngineFactory.createEngineDescription(ParagraphNewlineAnnotator.class),
                    AnalysisEngineFactory.createEngineDescription(CasDumpWriter.class));
        }
    }
}