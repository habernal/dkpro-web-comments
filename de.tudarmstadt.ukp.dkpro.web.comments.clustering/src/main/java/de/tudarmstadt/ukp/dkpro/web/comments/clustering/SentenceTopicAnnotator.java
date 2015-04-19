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

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;

/**
 * (c) 2015 Ivan Habernal
 */
public class SentenceTopicAnnotator
        extends JCasAnnotator_ImplBase
{
    private static final String NONE_LABEL = "X";

    public final static String PARAM_MODEL_LOCATION = "modelLocation";
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private File modelLocation;

    /**
     * The annotation type to use as tokens. Default: {@link de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token}
     */
    public final static String PARAM_TYPE_NAME = "typeName";
    @ConfigurationParameter(name = PARAM_TYPE_NAME, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String typeName;

    /**
     * The number of iterations during inference. Default: 10.
     */
    public final static String PARAM_N_ITERATIONS = "nIterations";
    @ConfigurationParameter(name = PARAM_N_ITERATIONS, mandatory = true, defaultValue = "10")
    private int nIterations;

    /**
     * The number of iterations before hyperparameter optimization begins. Default: 1
     */
    public final static String PARAM_BURN_IN = "burnIn";
    @ConfigurationParameter(name = PARAM_BURN_IN, mandatory = true, defaultValue = "1")
    private int burnIn;

    public final static String PARAM_THINNING = "thinning";
    @ConfigurationParameter(name = PARAM_THINNING, mandatory = true, defaultValue = "5")
    private int thinning;

    /**
     * If set, uses lemmas instead of original text as features.
     */
    public static final String PARAM_USE_LEMMA = "useLemma";
    @ConfigurationParameter(name = PARAM_USE_LEMMA, mandatory = true, defaultValue = "true")
    private boolean useLemma;

    private TopicInferencer inferencer;
    private Pipe malletPipe;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            ParallelTopicModel model = ParallelTopicModel.read(modelLocation);
            inferencer = model.getInferencer();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        malletPipe = new TokenSequence2FeatureSequence();
    }

    protected void annotateSentenceWithTopicDistribution(Sentence sentence, JCas jCas)
            throws AnalysisEngineProcessException
    {
        /* convert tokens (or other annotation type) into a Mallet TokenSequence */
        TokenSequence tokenStream = new TokenSequence();

        for (Token token : JCasUtil.selectCovered(Token.class, sentence)) {
            if (useLemma) {
                Lemma lemma = token.getLemma();
                if (lemma == null) {
                    throw new AnalysisEngineProcessException(
                            new IllegalStateException("Lemma = null; text not lemmatized?"));
                }
                tokenStream.add(lemma.getValue());
            }
            else {
                tokenStream.add(token.getCoveredText());
            }
        }

        // create Mallet Instance
        Instance instance = new Instance(tokenStream, NONE_LABEL, "emptyName", "emptyUri");

        // infer topic distribution for the sentence
        TopicDistribution topicDistributionAnnotation = new TopicDistribution(jCas);
        topicDistributionAnnotation.setBegin(sentence.getBegin());
        topicDistributionAnnotation.setEnd(sentence.getEnd());

        double[] topicDistribution = inferencer
                .getSampledDistribution(malletPipe.instanceFrom(instance), nIterations, thinning,
                        burnIn);

        // convert data type
        DoubleArray da = new DoubleArray(jCas, topicDistribution.length);
        da.copyFromArray(topicDistribution, 0, 0, topicDistribution.length);
        topicDistributionAnnotation.setTopicProportions(da);

        topicDistributionAnnotation.addToIndexes();
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
            annotateSentenceWithTopicDistribution(sentence, aJCas);
        }
    }
}