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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.dl.Embedding;
import de.tudarmstadt.ukp.dkpro.web.comments.clustering.dl.Word2VecReader;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;
import java.util.*;

/**
 * For each {@link de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence} in the input JCas,
 * it computes the accumulated embeddings vector (loaded from cache) and prints it to the output
 * file; the output file is in the Cluto format (heading, vectors)
 *
 * @author Ivan Habernal
 */
public class EmbeddingsClutoDataWriter
        extends JCasConsumer_ImplBase
{
    public static final String PARAM_WORD_2_VEC_FILE = "word2VecFile";

    @ConfigurationParameter(name = PARAM_WORD_2_VEC_FILE, mandatory = true,
            defaultValue = "/home/user-ukp/research/data/GoogleNews-vectors-negative300.bin")
    private File word2VecFile;

    public static final String PARAM_CACHE_FILE = "cacheFile";
    @ConfigurationParameter(name = PARAM_CACHE_FILE, mandatory = false)
    private File cacheFile;

    public static final String PARAM_OUTPUT_FOLDER = "outputFile";
    @ConfigurationParameter(name = PARAM_OUTPUT_FOLDER, mandatory = true)
    private File outputFile;

    private Word2VecReader reader;

    Map<String, Vector> cache = new HashMap<>();

    // fixme make dynamical
    public static final int VECTOR_SIZE = 300;
    private PrintWriter pwTemp;
    private File tmpFile;
    private int collectionSize = 0;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {

        super.initialize(context);

        try {
            if (cacheFile != null) {
                loadCache();
            }

            // temporary file and writer
            tmpFile = File.createTempFile("tmp_cluto", ".dat");
            pwTemp = new PrintWriter(tmpFile);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    private void initReader()
            throws IOException
    {
        if (reader == null) {
            reader = new Word2VecReader(word2VecFile, true);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCache()
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

    private Embedding getEmbeddings(String token)
    {

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

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        // process each sentence
        for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
            // get all embeddings
            List<String> tokens = new ArrayList<>();
            for (Token t : JCasUtil.selectCovered(Token.class, sentence)) {
                String coveredText = t.getCoveredText();
                if (coveredText.length() < 50) {
                    tokens.add(coveredText);
                }
            }

            Vector finalVector = new DenseVector(VECTOR_SIZE);
            for (String token : tokens) {
                Embedding embedding = getEmbeddings(token);

                if (embedding != null && embedding.getVector() != null) {
                    finalVector.add(embedding.getVector());
                }
            }

            // print final vector values
            for (int k = 0; k < finalVector.size(); k++) {
                double value = finalVector.get(k);
                pwTemp.printf(Locale.ENGLISH, "%f ", value);
            }
            pwTemp.println();

            collectionSize++;

            System.out.println(collectionSize + ", " + finalVector.get(0));
        }
    }



    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        // now produce output file
        // close temp file
        pwTemp.flush();
        pwTemp.close();

        try {
            PrintWriter pw = new PrintWriter(outputFile);

            pw.printf(Locale.ENGLISH, "%d %d%n", collectionSize, VECTOR_SIZE);

            // copy the rest
            IOUtils.copy(new FileInputStream(tmpFile), pw);

            pw.flush();
            pw.close();

            // delete tmp file
            FileUtils.deleteQuietly(tmpFile);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
