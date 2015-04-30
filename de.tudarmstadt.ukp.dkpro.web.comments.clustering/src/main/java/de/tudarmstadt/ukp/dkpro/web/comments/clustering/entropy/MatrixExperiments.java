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

import de.tudarmstadt.ukp.dkpro.web.comments.clustering.VectorUtils;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class MatrixExperiments
{
    public static void main(String[] args)
            throws Exception
    {
        File in = new File(args[0]);

        List<String> lines = IOUtils.readLines(new FileInputStream(in));

        int rows = lines.size();
        int cols = lines.iterator().next().split("\\s+").length;

        double[][] matrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            String line = lines.get(i);

            String[] split = line.split("\\s+");

            for (int j = 0; j < split.length; j++) {
                Double value = Double.valueOf(split[j]);

                matrix[i][j] = value;
            }

            // entropy of the cluster
            Vector v = new DenseVector(matrix[i]);
//            System.out.print(VectorUtils.entropy(v));
            System.out.print(VectorUtils.entropy(VectorUtils.normalize(v)));
            System.out.print(" ");
        }

        HeatChart map = new HeatChart(matrix);

        // Step 2: Customise the chart.
        map.setTitle("This is my heat chart title");
        map.setXAxisLabel("X Axis");
        map.setYAxisLabel("Y Axis");

        // Step 3: Output the chart to a file.
        map.saveToFile(new File("/tmp/java-heat-chart.png"));

    }

}
