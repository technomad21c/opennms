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
package org.opennms.features.apilayer.collectors;

import org.opennms.integration.api.v1.collectors.CollectorRequestBuilder;
import org.opennms.integration.api.v1.collectors.ServiceCollectorClient;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;

public class ServiceCollectorClientImpl implements ServiceCollectorClient {

    private final LocationAwareCollectorClient locationAwareCollectorClient;

    private final CollectionAgentFactory collectionAgentFactory;

    private final NodeDao nodeDao;


    public ServiceCollectorClientImpl(LocationAwareCollectorClient locationAwareCollectorClient, CollectionAgentFactory collectionAgentFactory, NodeDao nodeDao) {
        this.locationAwareCollectorClient = locationAwareCollectorClient;
        this.collectionAgentFactory = collectionAgentFactory;
        this.nodeDao = nodeDao;
    }

    @Override
    public CollectorRequestBuilder collect() {
        return new CollectorRequestBuilderImpl(locationAwareCollectorClient, collectionAgentFactory, nodeDao);
    }
}
