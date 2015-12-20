package org.opennms.netmgt.dao.api;


import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.topology.BroadcastDomain;

public interface BridgeTopologyDao {
    
    void delete(int nodeid);
    
    // The parse methods are used to check the Bridge Forwarding Table
    void parse(BridgeElement element);
    void parse(BridgeMacLink maclink);
    void parse(BridgeStpLink stpLink);
    void walked(int nodeid, Date now);

    // Storing is saving data without calculations
    void loadTopology(List<BridgeElement> bridgeelements, List<BridgeMacLink> bridgemaclinks,List<BridgeBridgeLink> bridgelinks, List<BridgeStpLink> stplinks);

    BroadcastDomain getBroadcastDomain(int nodeid);

}
