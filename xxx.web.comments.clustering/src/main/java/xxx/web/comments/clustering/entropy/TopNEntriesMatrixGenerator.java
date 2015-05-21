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

package xxx.web.comments.clustering.entropy;

import xxx.web.comments.clustering.VectorUtils;
import no.uib.cipr.matrix.Vector;

import java.util.Map;

/**
 * @author XXX
 */
public class TopNEntriesMatrixGenerator
        extends ClusterTopicMatrixGenerator
{
    @Override
    protected void updateClusterTopicMatrix(Vector distanceToClusterCentroidsVector,
            Vector topicDistributionVector)
    {
        int topNEntries = 5;

        for (Map.Entry<Double, Integer> entry : VectorUtils.largestValues(
                distanceToClusterCentroidsVector, topNEntries).entrySet()) {
            int cluster = entry.getValue();

            for (Map.Entry<Double, Integer> entry2 : VectorUtils
                    .largestValues(topicDistributionVector, topNEntries).entrySet()) {
                int topic = entry2.getValue();

                double value = entry.getKey() * entry2.getKey();

                clusterTopicMatrix.add(cluster, topic, value);
            }
        }
    }
}
