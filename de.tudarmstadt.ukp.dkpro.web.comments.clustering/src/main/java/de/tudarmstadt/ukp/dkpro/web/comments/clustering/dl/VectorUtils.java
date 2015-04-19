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

package de.tudarmstadt.ukp.dkpro.web.comments.clustering.dl;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import no.uib.cipr.matrix.VectorEntry;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Carsten Schnober
 */
public class VectorUtils
{
    public static SortedMap<Double, Integer> largestValues(Vector vector, int n)
    {
        TreeMap<Double, Integer> result = new TreeMap<>(new Comparator<Double>()
        {
            @Override
            public int compare(Double d1, Double d2)
            {
                return Double.compare(Math.abs(d1), Math.abs(d2));
            }
        });
        for (VectorEntry entry : vector) {
            result.put(entry.get(), entry.index());
        }
        while (result.size() > n) {
            result.remove(result.firstKey());
        }
        return result.descendingMap();
    }

    public static Vector crossProduct(Vector v1, Vector v2)
    {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("Vectors must be of same size!");
        }

        Vector result = new DenseVector(v1.size());

        for (int i = 0; i < result.size(); i++) {
            result.set(i, v1.get(i) * v2.get(i));
        }
        return result;
    }

    public static Vector crossDivide(Vector v1, Vector v2)
    {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("Vectors must be of same size!");
        }
        Vector result = new DenseVector(v1.size());

        for (int i = 0; i < result.size(); i++) {
            result.set(i, v1.get(i) / v2.get(i));
        }
        return result;
    }

    public static Vector scalePositive(Vector vector)
    {
        Vector result = vector.copy();
        for (VectorEntry entry : result) {
            entry.set((entry.get() + 1) / 2);
        }
        return result;
    }

    public static Vector unit(Vector vector)
    {
        return vector.scale(1 / vector.norm(Norm.Two));
    }
}
