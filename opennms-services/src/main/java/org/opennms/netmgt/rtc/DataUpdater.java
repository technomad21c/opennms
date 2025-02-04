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
package org.opennms.netmgt.rtc;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.events.api.model.IValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * The DataUpdater is created for each event by the event receiver. Depending on
 * the event UEI, relevant information is read from the event and the
 * DataManager informed so that data maintained by the RTC is kept up-to-date
 * </P>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
final class DataUpdater implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(DataUpdater.class);
    /**
     * The event from which data is to be read
     */
    private final IEvent m_event;
	private final DataManager m_dataManager;

    /**
     * If it is a nodeGainedService, create a new entry in the map
     */
    private void handleNodeGainedService(int nodeid, InetAddress ip, String svcName) {

        if (nodeid == -1 || ip == null || svcName == null) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/svc: {}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName);
            return;
        }

        m_dataManager.nodeGainedService(nodeid, ip, svcName);

        LOG.debug("{} added {}: {}: {} to data store", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName);

    }

    /**
     * If it is a outageCreated, update downtime on the rtcnode
     */
    private void handleOutageCreated(int nodeid, InetAddress ip, String svcName, long eventTime) {

        if (nodeid == -1 || ip == null || svcName == null || eventTime == -1) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/svc/eventtime: {}/{}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName, eventTime);
            return;
        }

        m_dataManager.outageCreated(nodeid, ip, svcName, eventTime);


        LOG.debug("Added outageCreated to nodeid: {} ip: {} svcName: {}", svcName, nodeid, InetAddressUtils.str(ip));
    }

    /**
     * If it is a outageResolved, update downtime on the rtcnode
     */
    private void handleOutageResolved(int nodeid, InetAddress ip, String svcName, long eventTime) {

        if (nodeid == -1 || ip == null || svcName == null || eventTime == -1) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/svc/eventtime: {}/{}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName, eventTime);
            return;
        }

        m_dataManager.outageResolved(nodeid, ip, svcName, eventTime);


        LOG.debug("Added outageResolved to nodeid: {} ip: {} svcName: {}", nodeid, InetAddressUtils.str(ip), svcName);
    }

    /**
     * If it is a serviceDeleted, remove corresponding RTC nodes from the map
     */
    private void handleServiceDeleted(int nodeid, InetAddress ip, String svcName) {

        if (nodeid == -1 || ip == null || svcName == null) {
            LOG.warn("{} ignored - info incomplete - nodeid/ip/svc: {}/{}/{}", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName);
            return;
        }

        m_dataManager.serviceDeleted(nodeid, ip, svcName);


        LOG.debug("{} deleted {}: {}: {} from data store", m_event.getUei(), nodeid, InetAddressUtils.str(ip), svcName);

    }

    /**
     * Record the interfaceReparented info in the datastore
     */
    private void handleInterfaceReparented(InetAddress ip, List<IParm> list) {

        if (ip == null || list == null) {
            LOG.warn("{} ignored - info incomplete - ip/parms: {}/{}", m_event.getUei(), InetAddressUtils.str(ip), list);
            return;
        }

        // old node ID
        int oldNodeId = -1;

        // new node ID
        int newNodeId = -1;

        String parmName = null;
        IValue parmValue = null;
        String parmContent = null;

        for (IParm parm : list) {
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null)
                continue;
            else
                parmContent = parmValue.getContent();

            // old node ID
            if (parmName.equals(EventConstants.PARM_OLD_NODEID)) {
                String temp = parmContent;
                try {
                    oldNodeId = Integer.parseInt(temp);
                } catch (NumberFormatException nfe) {
                    LOG.warn("Parameter {} cannot be non-numeric", EventConstants.PARM_OLD_NODEID, nfe);
                    oldNodeId = -1;
                }
            }

            // new node ID
            else if (parmName.equals(EventConstants.PARM_NEW_NODEID)) {
                String temp = parmContent;
                try {
                    newNodeId = Integer.parseInt(temp);
                } catch (NumberFormatException nfe) {
                    LOG.warn("Parameter {} cannot be non-numeric", EventConstants.PARM_NEW_NODEID, nfe);
                    newNodeId = -1;
                }
            }

        }

        if (oldNodeId == -1 || newNodeId == -1) {
            LOG.warn("{} did not have all required information for {} Values contained old nodeid: {} new nodeid: {}", m_event.getUei(), InetAddressUtils.str(ip), oldNodeId, newNodeId);
        } else {
            m_dataManager.interfaceReparented(ip, oldNodeId, newNodeId);

            LOG.debug("{} reparented ip: {} from {} to {}", m_event.getUei(), InetAddressUtils.str(ip), oldNodeId, newNodeId);

        }

    }

    /**
     * If it is a assetInfoChanged method, update RTC
     */
    private void handleAssetInfoChangedEvent(int nodeid) {

        m_dataManager.assetInfoChanged(nodeid);

        LOG.debug("{} asset info changed for node {}", m_event.getUei(), nodeid);

    }
    
    /**
     * If a node's surveillance category membership changed,
     * update RTC since RTC categories may include surveillance
     * categories via "categoryName" or "catinc*" rules
     */
    private void handleNodeCategoryMembershipChanged(int nodeid) {

        m_dataManager.nodeCategoryMembershipChanged(nodeid);

        LOG.debug("{} surveillance category membership changed for node {}", m_event.getUei(), nodeid);
    }

    /**
     * Read the event UEI, node ID, interface and service - depending on the UEI,
     * read event parms, if necessary, and call appropriate methods on the data
     * manager to update data
     */
    private void processEvent() {

        if (m_event == null) {
            LOG.debug("Event is null, nothing to process");
            return;
        }

        final boolean isPerspectiveNull = m_event.getParm("perspective") == null ? true : m_event.getParm("perspective").getValue() == null;

        if (!isPerspectiveNull) {
            LOG.trace("Event's perspective is not null, nothing to process");
            return;
        }

        String eventUEI = m_event.getUei();
        if (eventUEI == null) {
            // huh? should only get registered events

            LOG.debug("Event received with null UEI, ignoring event");
            return;
        }

        int nodeid = -1;
        if (m_event.hasNodeid()) {
            nodeid = m_event.getNodeid().intValue();
        }

        InetAddress ip = m_event.getInterfaceAddress();

        String svcName = m_event.getService();

        long eventTime = m_event.getTime().getTime();

        LOG.debug("Event UEI: {}\tnodeid: {}\tip: {}\tsvcName: {}\teventTime: {}", eventUEI, nodeid, InetAddressUtils.str(ip), svcName, eventTime);

        //
        //
        // Check for any of the following UEIs:
        //
        // nodeGainedService
        // outageCreated
        // outageResolved
        // serviceDeleted
        // interfaceReparented
        // subscribe
        // unsubscribe
        //
        if (eventUEI.equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)) {
            handleNodeGainedService(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.OUTAGE_CREATED_EVENT_UEI)) {
            handleOutageCreated(nodeid, ip, svcName, eventTime);
        } else if (eventUEI.equals(EventConstants.OUTAGE_RESOLVED_EVENT_UEI)) {
            handleOutageResolved(nodeid, ip, svcName, eventTime);
        } else if (eventUEI.equals(EventConstants.SERVICE_DELETED_EVENT_UEI)) {
            handleServiceDeleted(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.SERVICE_UNMANAGED_EVENT_UEI)) {
            handleServiceDeleted(nodeid, ip, svcName);
        } else if (eventUEI.equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
            handleInterfaceReparented(ip, m_event.getParmCollection());
        } else if (eventUEI.equals(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI)) {
            handleAssetInfoChangedEvent(nodeid);
        } else if (eventUEI.equals(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI)) {
            handleNodeCategoryMembershipChanged(nodeid);
        } else {
            LOG.debug("Event subscribed for not handled?!: {}", eventUEI);
        }
    }

    /**
     * Constructs the DataUpdater object
     * @param dataManager 
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    public DataUpdater(DataManager dataManager, IEvent event) {
    	m_dataManager = dataManager;
        m_event = event;
    }

    /**
     * Process the event depending on the UEI and update date
     */
    @Override
    public void run() {
        final Map<String,String> mdc = Logging.getCopyOfContextMap();

        try {
            Logging.putPrefix("rtc");
            processEvent();
        } catch (Throwable t) {
            LOG.warn("Unexpected exception processing event", t);
            Logging.setContextMap(mdc);
        }
    }
}
