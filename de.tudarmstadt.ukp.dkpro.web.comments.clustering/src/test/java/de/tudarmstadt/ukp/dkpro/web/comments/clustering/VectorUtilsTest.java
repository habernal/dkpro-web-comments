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

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Ivan Habernal
 */
public class VectorUtilsTest
{

    @Test
    public void testNormalize()
            throws Exception
    {
        Vector v = new DenseVector(new double[] { 1, 1, 1, 1, 1, 5 });
        assertNotEquals(1.0, v.norm(Vector.Norm.One), 0.0001);

        Vector normalize = VectorUtils.normalize(v);

        // sums up to 1.0
        assertEquals(1.0, normalize.norm(Vector.Norm.One), 0.0001);
    }
}