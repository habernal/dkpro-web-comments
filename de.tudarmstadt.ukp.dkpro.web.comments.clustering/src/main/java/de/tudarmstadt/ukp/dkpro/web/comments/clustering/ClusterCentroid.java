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
import no.uib.cipr.matrix.VectorEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * (c) 2015 Ivan Habernal
 */
public class ClusterCentroid
{

    public static final int EMBEDDINGS_VECTOR_SIZE = 300;

    public static void main(String[] args)
            throws Exception
    {
        TreeMap<Integer, Vector> centroids = computeClusterCentroids(args[0], args[1]);

//        System.out.println(centroids);
        embeddingsToDistance(args[0], centroids);


    }

    public static void embeddingsToDistance(String inputVectorsPath, TreeMap<Integer, Vector> centroids)
            throws IOException
    {
        // input for cluto
        File inputVectors = new File(inputVectorsPath);

        LineIterator vectorsIterator = IOUtils
                .lineIterator(new FileInputStream(inputVectors), "utf-8");

        // skip first line (number of clusters and vector size
        vectorsIterator.next();

        while (vectorsIterator.hasNext()) {
            String vectorString = vectorsIterator.next();

            // now parse the vector
            DenseVector vector = parseVector(vectorString);

            Vector distanceToClusterCentroidsVector = transformEmbeddingVectorToDistanceToClusterCentroidsVector(
                    vector, centroids);

            System.out.println(distanceToClusterCentroidsVector);
        }
    }


    public static Vector transformEmbeddingVectorToDistanceToClusterCentroidsVector(
            Vector embeddingVector, TreeMap<Integer, Vector> centroids)
    {
        Vector result = new DenseVector(centroids.size());

        double embeddingNorm = embeddingVector.norm(Vector.Norm.TwoRobust);

        // the centroids map is sorted and starts from 0
        for (int i = 0; i < centroids.size(); i++) {
            Vector centroid = centroids.get(i);

            // compute distance - cosine similarity
            double normCentroid = centroid.norm(Vector.Norm.TwoRobust);

            double distance = centroid.dot(embeddingVector) / (normCentroid * embeddingNorm);

            result.set(i, distance);
        }

        return result;
    }

    public static TreeMap<Integer, Vector> computeClusterCentroids(String inputVectorsPath,
            String clusterOutputPath)
            throws IOException
    {
        TreeMap<Integer, Vector> result = new TreeMap<>();
        Map<Integer, Integer> counts = new TreeMap<>();

        // input for cluto
        File inputVectors = new File(inputVectorsPath);

        // resulting clusters
        File clutoClustersOutput = new File(clusterOutputPath);

        LineIterator clustersIterator = IOUtils
                .lineIterator(new FileInputStream(clutoClustersOutput), "utf-8");

        LineIterator vectorsIterator = IOUtils
                .lineIterator(new FileInputStream(inputVectors), "utf-8");

        // skip first line (number of clusters and vector size
        vectorsIterator.next();

        while (clustersIterator.hasNext()) {
            String clusterString = clustersIterator.next();
            String vectorString = vectorsIterator.next();

            int clusterNumber = Integer.valueOf(clusterString);

            // now parse the vector
            DenseVector vector = parseVector(vectorString);

            // if there is no resulting vector for the particular cluster, add this one
            if (!result.containsKey(clusterNumber)) {
                result.put(clusterNumber, vector);
            }
            else {
                // otherwise add this one to the previous one
                result.put(clusterNumber, result.get(clusterNumber).add(vector));
            }

            // and update counts
            if (!counts.containsKey(clusterNumber)) {
                counts.put(clusterNumber, 0);
            }

            counts.put(clusterNumber, counts.get(clusterNumber) + 1);
        }

        // now compute average for each vector
        for (Map.Entry<Integer, Vector> entry : result.entrySet()) {
            // cluster number
            int clusterNumber = entry.getKey();
            // get counts
            int count = counts.get(clusterNumber);

            // divide by count of vectors for each cluster (averaging)
            for (VectorEntry vectorEntry : entry.getValue()) {
                vectorEntry.set(vectorEntry.get() / (double) count);
            }
        }

        return result;
    }

    public static DenseVector parseVector(String line)
    {
        DenseVector result = new DenseVector(EMBEDDINGS_VECTOR_SIZE);
        String[] tokens = line.split("\\s+");
        if (tokens.length != EMBEDDINGS_VECTOR_SIZE) {
            throw new IllegalArgumentException("Vector size mismatch");
        }

        for (int i = 0; i < tokens.length; i++) {
            result.set(i, Double.valueOf(tokens[i]));
        }

        return result;
    }
}
