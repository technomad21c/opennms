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
package org.opennms.netmgt.flows.classification.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationRequestBuilder;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.flows.classification.internal.value.IpRange;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.ProtocolType;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

import com.google.common.collect.Lists;

public class DefaultClassificationEngineTest {

    private static ClassificationRequest classificationRequest(String location, int srcPort, String srcAddress, int dstPort, String dstAddress, Protocol protocol) {
        return new ClassificationRequest(location, srcPort, IpAddr.of(srcAddress), dstPort, IpAddr.of(dstAddress), protocol);
    }

    @Test
    public void verifyRuleEngineBasic() throws InterruptedException {
        DefaultClassificationEngine engine = new DefaultClassificationEngine(() ->
            Lists.newArrayList(
                    new RuleBuilder().withName("rule1").withPosition(1).withSrcPort(80).build(),
                    new RuleBuilder().withName("rule2").withPosition(2).withDstPort(443).build(),
                    new RuleBuilder().withName("rule3").withPosition(3).withSrcPort(8888).withDstPort(9999).build(),
                    new RuleBuilder().withName("rule4").withPosition(4).withSrcPort(8888).withDstPort(80).build(),
                    new RuleBuilder().withName("rule5").withPosition(5).build()
            ), FilterService.NOOP);

        assertEquals("rule2", engine.classify(new ClassificationRequestBuilder().withSrcPort(9999).withDstPort(443).build()));
        assertEquals("rule3", engine.classify(new ClassificationRequestBuilder().withSrcPort(8888).withDstPort(9999).build()));
        assertEquals("rule4", engine.classify(new ClassificationRequestBuilder().withSrcPort(8888).withDstPort(80).build()));
    }

    @Test
    public void verifyRuleEngineWithOmnidirectionals() throws InterruptedException {
        DefaultClassificationEngine engine = new DefaultClassificationEngine(() ->
                Lists.newArrayList(
                        new RuleBuilder().withName("rule1").withSrcPort(80).withOmnidirectional(true).build(),
                        new RuleBuilder().withName("rule2").withDstPort(443).withOmnidirectional(true).build(),
                        new RuleBuilder().withName("rule3").withSrcPort(8080).withDstPort(8443).withOmnidirectional(true).build(),
                        new RuleBuilder().withName("rule4").withSrcPort(1337).build(),
                        new RuleBuilder().withName("rule5").withDstPort(7331).build()
                ), FilterService.NOOP);

        assertEquals("rule1", engine.classify(new ClassificationRequestBuilder().withSrcPort(9999).withDstPort(80).build()));
        assertEquals("rule1", engine.classify(new ClassificationRequestBuilder().withSrcPort(80).withDstPort(9999).build()));

        assertEquals("rule2", engine.classify(new ClassificationRequestBuilder().withSrcPort(443).withDstPort(9999).build()));
        assertEquals("rule2", engine.classify(new ClassificationRequestBuilder().withSrcPort(9999).withDstPort(443).build()));

        assertEquals("rule3", engine.classify(new ClassificationRequestBuilder().withSrcPort(8080).withDstPort(8443).build()));
        assertEquals("rule3", engine.classify(new ClassificationRequestBuilder().withSrcPort(8443).withDstPort(8080).build()));

        assertEquals("rule4", engine.classify(new ClassificationRequestBuilder().withSrcPort(1337).withDstPort(9999).build()));
        assertNull(engine.classify(new ClassificationRequestBuilder().withSrcPort(9999).withDstPort(1337).build()));

        assertEquals("rule5", engine.classify(new ClassificationRequestBuilder().withSrcPort(9999).withDstPort(7331).build()));
        assertNull(engine.classify(new ClassificationRequestBuilder().withSrcPort(7331).withDstPort(9999).build()));
    }

    @Test
    public void verifyRuleEngineExtended() throws InterruptedException {
        // Define Rule set
        DefaultClassificationEngine engine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new RuleBuilder().withName("SSH").withDstPort("22").withPosition(1).build(),
                new RuleBuilder().withName("HTTP_CUSTOM").withDstAddress("192.168.0.1").withDstPort("80").withPosition(2).build(),
                new RuleBuilder().withName("HTTP").withDstPort("80").withPosition(3).build(),
                new RuleBuilder().withName("DUMMY").withDstAddress("192.168.1.0-192.168.1.255,10.10.5.3,192.168.0.0/24").withDstPort("8000-9000,80,8080").withPosition(4).build(),
                new RuleBuilder().withName("RANGE-TEST").withDstPort("7000-8000").withPosition(5).build(),
                new RuleBuilder().withName("OpenNMS").withDstPort("8980").withPosition(6).build(),
                new RuleBuilder().withName("OpenNMS Monitor").withDstPort("1077").withSrcPort("5347").withSrcAddress("10.0.0.5").withPosition(7).build()
            ), FilterService.NOOP
        );

        // Verify concrete mappings
        assertEquals("SSH",         engine.classify(classificationRequest("Default", 0, null,  22, "127.0.0.1", ProtocolType.TCP)));
        assertEquals("HTTP_CUSTOM", engine.classify(classificationRequest("Default", 0, null, 80, "192.168.0.1", ProtocolType.TCP)));
        assertEquals("HTTP",        engine.classify(classificationRequest("Default", 0, null, 80, "192.168.0.2", ProtocolType.TCP)));
        assertEquals(null,          engine.classify(classificationRequest("Default", 0, null, 5000, "localhost", ProtocolType.UDP)));
        assertEquals(null,          engine.classify(classificationRequest("Default", 0, null, 5000, "localhost", ProtocolType.TCP)));
        assertEquals("OpenNMS",     engine.classify(classificationRequest("Default", 0, null, 8980, "127.0.0.1", ProtocolType.TCP)));
        assertEquals("OpenNMS Monitor", engine.classify(
                new ClassificationRequestBuilder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(1077)
                        .withProtocol(ProtocolType.TCP).build()));
        assertEquals("OpenNMS Monitor", engine.classify(
                new ClassificationRequestBuilder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(1077)
                        .withDstAddress("192.168.0.2")
                        .withProtocol(ProtocolType.TCP).build()));
        assertEquals("HTTP", engine.classify(
                new ClassificationRequestBuilder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(80)
                        .withDstAddress("192.168.0.2")
                        .withProtocol(ProtocolType.TCP).build()));
        assertEquals("DUMMY", engine.classify(new ClassificationRequestBuilder()
                .withLocation("Default")
                .withSrcAddress("127.0.0.1")
                .withDstAddress("10.10.5.3")
                .withSrcPort(5213)
                .withDstPort(8080)
                .withProtocol(ProtocolType.TCP).build()));

        // Verify IP Range
        var ipAddresses = IpRange.of("192.168.1.0", "192.168.1.255");
        for (var ipAddress : ipAddresses) {
            final ClassificationRequest classificationRequest = new ClassificationRequest("Default", 0, null, 8080, ipAddress, ProtocolType.TCP);
            assertEquals("DUMMY", engine.classify(classificationRequest));

            // Populate src address and port. Result must be the same
            classificationRequest.setSrcAddress("10.0.0.1");
            classificationRequest.setSrcPort(5123);
            assertEquals("DUMMY", engine.classify(classificationRequest));
        }

        // Verify CIDR expression
        for (var ipAddress : IpRange.of("192.168.0.0", "192.168.0.255")) {
            final ClassificationRequest classificationRequest = new ClassificationRequest("Default", 0, null, 8080, ipAddress, ProtocolType.TCP);
            assertEquals("DUMMY", engine.classify(classificationRequest));
        }

        // Verify Port Range
        IntStream.range(7000, 8000).forEach(i -> assertEquals("RANGE-TEST", engine.classify(classificationRequest("Default", 0, null,  i, "192.168.0.2", ProtocolType.TCP))));

        // Verify Port Range with Src fields populated. Result must be the same
        IntStream.range(7000, 8000).forEach(src -> {
            IntStream.range(7000, 8000).forEach(dst -> {
                final ClassificationRequest classificationRequest = new ClassificationRequestBuilder()
                        .withLocation("Default")
                        .withProtocol(ProtocolType.TCP)
                        .withSrcAddress("10.0.0.1").withSrcPort(src)
                        .withDstAddress("192.168.0.2").withDstPort(dst).build();
                assertEquals("RANGE-TEST", engine.classify(classificationRequest));
            });
        });
    }

    @Test
    public void verifyAddressRuleWins() throws InterruptedException {
        final ClassificationEngine engine = new DefaultClassificationEngine(() -> Lists.newArrayList(
            new RuleBuilder().withName("HTTP").withDstPort(80).withPosition(1).build(),
            new RuleBuilder().withName("XXX2").withSrcAddress("192.168.2.1").withSrcPort(4789).build(),
            new RuleBuilder().withName("XXX").withDstAddress("192.168.2.1").build()
        ), FilterService.NOOP);

        final ClassificationRequest classificationRequest = classificationRequest("Default", 0, null, 80, "192.168.2.1", ProtocolType.TCP);
        assertEquals("XXX", engine.classify(classificationRequest));
        assertEquals("XXX2", engine.classify(new ClassificationRequestBuilder()
                .withLocation("Default")
                .withProtocol(ProtocolType.TCP)
                .withSrcAddress("192.168.2.1").withSrcPort(4789)
                .withDstAddress("52.31.45.219").withDstPort(80)
                .build()));
    }

    @Test
    public void verifyAllPortsToEnsureEngineIsProperlyInitialized() throws InterruptedException {
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(() -> new ArrayList<>(), FilterService.NOOP);
        for (int i=Rule.MIN_PORT_VALUE; i<Rule.MAX_PORT_VALUE; i++) {
            classificationEngine.classify(classificationRequest("Default", 0, null, i, "127.0.0.1", ProtocolType.TCP));
        }
    }

    // See NMS-12429
    @Test
    public void verifyDoesNotRunOutOfMemory() throws InterruptedException {
        final List<Rule> rules = Lists.newArrayList();
        for (int i=0; i<100; i++) {
            final Rule rule = new RuleBuilder().withName("rule1").withPosition(i+1).withProtocol("UDP").withDstAddress("192.168.0." + i).build();
            rules.add(rule);
        }
        final DefaultClassificationEngine engine = new DefaultClassificationEngine(() -> rules, FilterService.NOOP);
        engine.classify(classificationRequest("localhost", 1234, "127.0.0.1", 80, "192.168.0.1", Protocols.getProtocol("UDP")));
    }

    @Test(timeout=5000)
    public void verifyInitializesQuickly() throws InterruptedException {
        new DefaultClassificationEngine(() -> Lists.newArrayList(new Rule("Test", "0-10000")), FilterService.NOOP);
    }
}
