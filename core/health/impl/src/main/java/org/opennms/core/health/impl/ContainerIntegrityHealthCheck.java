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
package org.opennms.core.health.impl;

import static org.opennms.core.health.api.HealthCheckConstants.BUNDLE;
import static org.opennms.core.health.api.HealthCheckConstants.LOCAL;
import static org.opennms.core.health.api.Status.Failure;
import static org.opennms.core.health.api.Status.Starting;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.karaf.bundle.core.BundleInfo;
import org.apache.karaf.bundle.core.BundleService;
import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.Health;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Verifies the integrity of the container.
 * This is achieved by iterating over all bundles and checking its state.
 * Every bundle which is not ACTIVE is considered failed.
 *
 * The only exceptions are:
 *  - fragment bundles as those never reach ACTIVE state and are considered successful if they are RESOLVED.
 *  - stopped bundles as they are very likely manually stopped or installed but not automatically started
 *
 * @author mvrueden
 */
public class ContainerIntegrityHealthCheck implements HealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(ContainerIntegrityHealthCheck.class);
    private final BundleService bundleService;
    private final BundleContext bundleContext;
    private final List<String> ignoreBundles;

    public ContainerIntegrityHealthCheck(BundleContext bundleContext, BundleService bundleService, String ignoreBundleList) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.bundleService = Objects.requireNonNull(bundleService);
        this.ignoreBundles = parse(ignoreBundleList);
    }

    @Override
    public String getDescription() {
        return "Verifying installed bundles";
    }

    @Override
    public List<String> getTags() {
        return Arrays.asList(LOCAL, BUNDLE);
    }

    @Override
    public Response perform(Context context) {
        // Don't check within this delay period, because the container may not be started yet
        if (ManagementFactory.getRuntimeMXBean().getUptime() <= 10000) {
            return new Response(Starting, "Container is in spin up phase");
        }

        // Verify all bundles
        final Health health = new Health();
        for (Bundle b : bundleContext.getBundles()) {
            if (ignoreBundles.contains(b.getSymbolicName())) {
                LOG.debug("Bundle {} with symbolic name {} is ignored while performing opennms:health-check", b.getBundleId(), b.getSymbolicName());
                continue;
            }
            final BundleInfo info = bundleService.getInfo(b);
            switch (info.getState()) {
                // Success
                case Active:
                    break;
                // only success if bundle is a fragment bundle
                case Resolved:
                    if ((b.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
                        break;
                    }
                    health.add(this, new Response(Failure, "Bundle " + b.getBundleId() + " is resolved, but not active"));
                    break;
                // Waiting for dependencies
                case Waiting:
                case GracePeriod:
                    health.add(this, new Response(Starting, "Bundle " + b.getBundleId() + " is waiting for dependencies"));
                    break;
                // Installed, but not yet started
                case Installed:
                    health.add(this, new Response(Starting, "Bundle " + b.getBundleId() + " is not yet started"));
                    break;
                // Starting
                case Starting:
                    health.add(this, new Response(Starting, "Bundle " + b.getBundleId() + " is starting"));
                    break;
                // Stopping, Failed ur Unknown are considered Failures
                case Stopping:
                case Failure:
                case Unknown:
                    health.add(this, new Response(Failure, "Bundle " + b.getBundleId() + " is not started"));
                    break;
            }
        }

        // If there are some issues, we return the worst one, otherwise everything is okay
        return health.getWorst().map(Pair::getRight).orElse(new Response(Status.Success));
    }

    private List<String> parse(String bundlesToIgnore) {
        if (Strings.isNullOrEmpty(bundlesToIgnore)) {
            return Collections.emptyList();
        }
        final Set<String> symbolicBundleNamesToIgnore = Arrays.stream(bundlesToIgnore.split(","))
                .map(s -> s == null ? null : s.trim()) // remove spaces, to catch "x, y" or " x, y"
                .filter(s -> s != null && !s.isEmpty()) // remove possible empty values, e.g. "x,,y"
                .collect(Collectors.toSet());// remove duplicates
        return new ArrayList<>(symbolicBundleNamesToIgnore);
    }
}
