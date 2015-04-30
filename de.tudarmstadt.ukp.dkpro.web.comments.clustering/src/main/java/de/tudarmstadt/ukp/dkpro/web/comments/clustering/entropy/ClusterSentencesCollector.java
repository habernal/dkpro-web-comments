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
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.ClusterCentroidsMain;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.ClusteringUtils;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.VectorUtils;
import de.tudarmstadt.ukp.dkpro.web.comments.type.Embeddings;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ivan Habernal
 */
public class ClusterSentencesCollector
        extends JCasConsumer_ImplBase
{
    /**
     * Output from {@link ClusterCentroidsMain}
     */
    public static final String PARAM_CENTROIDS_FILE = "centroidsFile";
    @ConfigurationParameter(name = PARAM_CENTROIDS_FILE, mandatory = true)
    File centroidsFile;

    public static final String PARAM_OUTPUT_DIR = "outputDir";
    @ConfigurationParameter(name = PARAM_OUTPUT_DIR, mandatory = false)
    File outputDir;

    TreeMap<Integer, Vector> centroids;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
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
            List<Embeddings> embeddingsList = JCasUtil.selectCovered(Embeddings.class, sentence);

            if (embeddingsList.size() != 1) {
                throw new AnalysisEngineProcessException(new IllegalStateException(
                        "Expected 1 embedding annotations for sentence, but " +
                                embeddingsList.size() + " found." +
                                "Sentence: " + sentence.getBegin() + sentence.getEnd() + ", "
                                + StringUtils.join(embeddingsList.iterator(), "\n")));
            }

            Embeddings embeddings = embeddingsList.iterator().next();
            DenseVector embeddingsVector = new DenseVector(embeddings.getVector().toArray());

            Vector distanceToClusterCentroidsVector = ClusteringUtils
                    .transformEmbeddingVectorToDistanceToClusterCentroidsVector(
                            embeddingsVector, centroids);

            Map.Entry<Double, Integer> entry = VectorUtils
                    .largestValues(distanceToClusterCentroidsVector, 1)
                    .entrySet().iterator().next();
            int cluster = entry.getValue();
            double distance = entry.getKey();

            try {
                appendSentence(cluster, distance, sentence.getCoveredText());
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    private void appendSentence(int cluster, double distance, String coveredText)
            throws IOException
    {
        String fileName = String.format(Locale.ENGLISH, "%3d.txt", cluster);
        File file = new File(outputDir, fileName);

        FileUtils.write(file, String.format(Locale.ENGLISH, "%.4f\t%s%n", distance, coveredText),
                true);
    }
}