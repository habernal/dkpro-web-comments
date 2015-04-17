package de.tudarmstadt.ukp.dkpro.web.comments.uima;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivan Habernal
 */
public class CharacterFrequencyCounter
        extends JCasAnnotator_ImplBase
{
    Map<Character, Integer> chars;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        chars = new HashMap<>();
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        for (int i = 0; i < aJCas.getDocumentText().length(); i++) {
            char ch = aJCas.getDocumentText().charAt(i);

            if (!chars.containsKey(ch)) {
                chars.put(ch, 0);
            }

            chars.put(ch, chars.get(ch) + 1);
        }
    }

    @Override public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        System.out.println(chars);
    }
}
