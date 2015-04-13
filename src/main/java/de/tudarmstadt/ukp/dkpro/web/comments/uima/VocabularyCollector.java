package de.tudarmstadt.ukp.dkpro.web.comments.uima;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Ivan Habernal
 */
public class VocabularyCollector
        extends JCasConsumer_ImplBase
{
    /**
     * File for storing vocabulary
     */
    public static final String PARAM_MODEL_LOCATION = "modelLocation";

    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    File modelLocation;

    public static final String PARAM_MINIMAL_OCCURRENCE = "minimalOccurrence";
    @ConfigurationParameter(name = PARAM_MINIMAL_OCCURRENCE, mandatory = false)
    int minimalOccurrence;

    public static final String PARAM_USE_LEMMA = "useLemma";
    @ConfigurationParameter(name = PARAM_USE_LEMMA, mandatory = true, defaultValue = "false")
    boolean useLemma;

    Map<String, Integer> vocabulary = new HashMap<>();

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        if (minimalOccurrence < 0) {
            throw new ResourceInitializationException(new IllegalArgumentException(
                    "Minimal occurrence must be positive integer"));
        }
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        for (Token t : JCasUtil.select(aJCas, Token.class)) {
            String entry = useLemma ? t.getCoveredText() : t.getLemma().getValue();

            // only words
            if (entry.matches("\\p{Alpha}+")) {
                if (!vocabulary.containsKey(entry)) {
                    vocabulary.put(entry, 0);
                }

                vocabulary.put(entry, vocabulary.get(entry) + 1);
            }
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        getLogger().info("Original vocabulary size: " + this.vocabulary.size());

        // remove all with low occurrence
        Iterator<Map.Entry<String, Integer>> iterator = vocabulary.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> next = iterator.next();

            // remove
            if (next.getValue() < this.minimalOccurrence) {
                iterator.remove();
            }
        }

        getLogger().info("Filtered vocabulary size: " + this.vocabulary.size());

        // serialize to file
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(this.modelLocation));
            oos.writeObject(vocabulary);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
