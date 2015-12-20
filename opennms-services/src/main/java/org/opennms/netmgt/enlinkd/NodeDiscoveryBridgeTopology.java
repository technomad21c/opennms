package org.opennms.netmgt.enlinkd;

import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDiscoveryBridgeTopology extends NodeDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryBridgeTopology.class);

    public NodeDiscoveryBridgeTopology(EnhancedLinkd linkd, Node node) {
        super(linkd, node);
        setInitialSleepTime(linkd.getInitialSleepTime()+180000);
    }

    @Override
    protected void runCollection() {
        
        BroadcastDomain domain = m_linkd.getQueryManager().getBridgeTopologyBroadcastDomain(getNodeId());
        if (domain == null ) {
            LOG.warn("run: no broadcast domain found for node: {}", getNodeId());
            return;
        }
        if (domain.isCalculating()) {
            LOG.info("run: broadcast domain is calculating either on node {}....", getNodeId());
            return;
        }
        if (!domain.isTopologyChanged()) {
            LOG.info("run: broadcast domain with no topology change found for node: {}. exiting...", getNodeId());
            return;
        }
        LOG.info("run: calculating broadcast domain with topology change found for node: {}.", getNodeId());
        domain.calculate();
        LOG.info("run: broadcast domain topology for node: {}. calculated.", getNodeId());
        LOG.info("run: saving broadcast domain topology for node: {}.", getNodeId());
        m_linkd.getQueryManager().store(domain);
        LOG.info("run: saved broadcast domain topology for node: {}.", getNodeId());
    }

    @Override
    public String getInfo() {
        return "ReadyRunnable DiscoveryBridgeTopology" + " node=" + getNodeId();
    }

    @Override
    public String getName() {
        return "DiscoveryBridgeTopology";
    }

}
