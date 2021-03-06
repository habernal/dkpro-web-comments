/*
 * Copyright 2015 XXX
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

package xxx.web.comments.clustering.embeddings;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * (c) 2015 XXX
 */
public class WholeDocumentEmbeddingsAnnotator
        extends EmbeddingsAnnotator
{

    @Override
    protected Collection<? extends Annotation> selectAnnotationsForEmbeddings(JCas aJCas)
    {
        // create dummy annotation
        Paragraph p = new Paragraph(aJCas, 0, aJCas.getDocumentText().length());
        p.addToIndexes();

        List<Annotation> result = new ArrayList<>();
        result.add(p);

        return result;
    }
}
