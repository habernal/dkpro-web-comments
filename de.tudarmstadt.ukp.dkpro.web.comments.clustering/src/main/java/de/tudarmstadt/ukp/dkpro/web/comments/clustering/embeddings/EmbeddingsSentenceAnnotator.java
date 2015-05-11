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

import de.tudarmstadt.ukp.dkpro.web.comments.type.Embeddings;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import java.util.List;

/**
 * For each {@link de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence} in the input JCas,
 * it computes the accumulated embeddings vector (loaded from cache) and creates a new annotation
 * of type {@link Embeddings} spanning the sentence.
 *
 * @author Ivan Habernal
 */
public class EmbeddingsSentenceAnnotator
        extends EmbeddingsAnnotator
{

    @Override
    protected DenseVector createFinalVector(List<Vector> vectors)
    {
        DenseVector result = new DenseVector(VECTOR_SIZE);

        for (Vector v : vectors) {
            result.add(v);
        }

        // averaging
        result.scale(1.0 / (double) vectors.size());

        return result;
    }
}
