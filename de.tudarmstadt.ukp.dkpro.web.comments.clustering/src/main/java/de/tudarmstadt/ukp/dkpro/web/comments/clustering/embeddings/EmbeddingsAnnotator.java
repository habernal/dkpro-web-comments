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

package de.tudarmstadt.ukp.dkpro.web.comments.clustering.embeddings;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.EmbeddingsCachePreprocessor;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.dl.Embedding;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.dl.Word2VecReader;
import de.tudarmstadt.ukp.dkpro.web.comments.type.Embeddings;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * (c) 2015 Ivan Habernal
 */
public class EmbeddingsAnnotator
        extends JCasAnnotator_ImplBase
{
    public static final String PARAM_WORD_2_VEC_FILE = "word2VecFile";

    @ConfigurationParameter(name = PARAM_WORD_2_VEC_FILE, mandatory = true,
            defaultValue = "/home/user-ukp/research/data/GoogleNews-vectors-negative300.bin")
    protected File word2VecFile;

    public static final String PARAM_CACHE_FILE = "cacheFile";
    @ConfigurationParameter(name = PARAM_CACHE_FILE, mandatory = false)
    protected File cacheFile;

    protected Word2VecReader reader;

    protected Map<String, Vector> cache = new HashMap<>();

    // fixme make dynamical
    public static final int VECTOR_SIZE = 300;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            if (cacheFile != null) {
                loadCache();
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    protected void initReader()
            throws IOException
    {
        if (reader == null) {
            reader = new Word2VecReader(word2VecFile, true);
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadCache()
            throws IOException
    {

        FileInputStream fis = new FileInputStream(cacheFile);
        ObjectInputStream os = new ObjectInputStream(fis);
        try {
            this.cache = (Map<String, Vector>) os.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        IOUtils.closeQuietly(fis);
    }

    protected Embedding getEmbeddings(String t)
    {
        String token = preprocessToken(t);
        if (!cache.containsKey(token)) {
            System.err.println("Word " + token + " not cached; maybe you forgot to run "
                    + EmbeddingsCachePreprocessor.class + " to prepare the cache?");

            if (token.length() < 50) {
                Embedding[] embeddings;
                try {
                    initReader();

                    embeddings = reader.getEmbeddings(token);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return embeddings[0];
            }

            return new Embedding(token, null);
        }
        else {
            return new Embedding(token, cache.get(token));
        }
    }

    /**
     * Normalizes the token, e.g. lower casing (no change by default)
     *
     * @param token token
     * @return token
     */
    protected String preprocessToken(String token)
    {
        return token;
    }

    protected Collection<? extends Annotation> selectAnnotationsForEmbeddings(JCas aJCas)
    {
        return JCasUtil.select(aJCas, Sentence.class);
    }

    protected DenseVector createFinalVector(List<Vector> vectors) {
        DenseVector result = new DenseVector(VECTOR_SIZE);

        for (Vector v : vectors) {
            result.add(v);
        }

        return result;
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        // process each annotation (sentence, etc.)
        for (Annotation annotation : selectAnnotationsForEmbeddings(aJCas)) {
            // get all embeddings
            List<String> tokens = new ArrayList<>();
            for (Token t : JCasUtil.selectCovered(Token.class, annotation)) {
                String coveredText = t.getCoveredText();
                if (coveredText.length() < 50) {
                    tokens.add(coveredText);
                }
            }

            List<Vector> vectors = new ArrayList<>();

            for (String token : tokens) {
                Embedding embedding = getEmbeddings(token);

                if (embedding != null && embedding.getVector() != null) {
                    vectors.add(embedding.getVector());
                }
            }

            DenseVector finalVector = createFinalVector(vectors);

            // make new annotation
            Embeddings embeddings = new Embeddings(aJCas, annotation.getBegin(),
                    annotation.getEnd());

            // copy double values
            DoubleArray doubleArray = new DoubleArray(aJCas, VECTOR_SIZE);
            doubleArray.copyFromArray(finalVector.getData(), 0, 0, VECTOR_SIZE);
            embeddings.setVector(doubleArray);

            embeddings.addToIndexes();
        }
    }
}