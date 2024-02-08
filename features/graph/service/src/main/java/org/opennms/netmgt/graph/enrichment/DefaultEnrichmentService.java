/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.graph.enrichment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.enrichment.EnrichmentGraphBuilder;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentProcessor;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentService;
import org.opennms.netmgt.graph.api.generic.GenericGraph;

public class DefaultEnrichmentService implements EnrichmentService {

    private final List<EnrichmentProcessor> enrichmentProcessors = new CopyOnWriteArrayList<>();

    @Override
    public GenericGraph enrich(GenericGraph graph) {
        if (graph != null) {
            final List<EnrichmentProcessor> actualProcessors = enrichmentProcessors.stream().filter(p -> p.canEnrich(graph)).collect(Collectors.toList());
            final EnrichmentGraphBuilder enrichmentGraphBuilder = new EnrichmentGraphBuilder(graph);
            for (EnrichmentProcessor processor : actualProcessors) {
                processor.enrich(enrichmentGraphBuilder);
            }
            final GenericGraph enrichedGraph = enrichmentGraphBuilder.build();
            return enrichedGraph;
        }
        return null;
    }

    public void onBind(EnrichmentProcessor enrichmentProcessor, Map<String, String> props) {
        enrichmentProcessors.add(enrichmentProcessor);
    }

    public void onUnbind(EnrichmentProcessor enrichmentProcessor, Map<String, String> props) {
        enrichmentProcessors.remove(enrichmentProcessor);
    }

}
