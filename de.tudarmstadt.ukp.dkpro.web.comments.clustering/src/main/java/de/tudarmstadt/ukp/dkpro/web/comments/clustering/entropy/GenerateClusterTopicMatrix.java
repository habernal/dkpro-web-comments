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

package de.tudarmstadt.ukp.dkpro.web.comments.clustering.entropy;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import no.uib.cipr.matrix.Vector;
import org.apache.commons.io.LineIterator;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ivan Habernal
 */
public class GenerateClusterTopicMatrix
        extends JCasConsumer_ImplBase
{
    /**
     * Output from {@link de.tudarmstadt.ukp.dkpro.web.comments.clustering.ClusterCentroid}
     */
    public static final String PARAM_CENTROIDS_FILE = "centroidsFile";
    @ConfigurationParameter(name = PARAM_CENTROIDS_FILE, mandatory = true)
    File centroidsFile;

    /**
     * Output mapping (debate -> topic distribution) from {@link de.tudarmstadt.ukp.dkpro.web.comments.clustering.topic.DebateTopicExtractor}
     */
    public static final String PARAM_DEBATE_TOPIC_MAP_FILE = "debateTopicMapFile";
    @ConfigurationParameter(name = PARAM_DEBATE_TOPIC_MAP_FILE, mandatory = true)
    File debateTopicMapFile;

    /**
     * Where the final matrix model is stored
     */
    public static final String PARAM_OUTPUT_MODEL_FILE = "outputModelFile";
    @ConfigurationParameter(name = PARAM_OUTPUT_MODEL_FILE, mandatory = true)
    File outputModelFile;

    Map<String, List<Double>> debateTopicMap;

    TreeMap<Integer, Vector> centroids;

    private LineIterator lineIterator;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            // load mapping debateURL -> topic distribution
            debateTopicMap = (Map<String, List<Double>>) new ObjectInputStream(
                    new FileInputStream(debateTopicMapFile)).readObject();

            // load centroids
            centroids = (TreeMap<Integer, Vector>) new ObjectInputStream(
                    new FileInputStream(centroidsFile)).readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        // iterate over sentences
        for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
            // and load the appropriate distance to centroids
        }
    }
}
