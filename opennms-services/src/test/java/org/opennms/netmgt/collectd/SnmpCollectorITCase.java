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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.mock.MockDataCollectionConfig;
import org.opennms.netmgt.mock.OpenNMSITCase;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl;
import org.springframework.core.io.ClassPathResource;

public abstract class SnmpCollectorITCase extends OpenNMSITCase {

	private static final class AttributeVerifier extends AbstractCollectionSetVisitor {
		private final List<MibObject> list;

		public int attributeCount = 0;
		private AttributeVerifier(List<MibObject> list) {
			this.list = list;
		}

		@Override
		public void visitAttribute(CollectionAttribute attribute) {
			visitAttribute((SnmpAttribute)attribute);
		}

		public void visitAttribute(SnmpAttribute attribute) {
			attributeCount++;
			assertMibObjectPresent(attribute, list);
		}
	}

    public MockDataCollectionConfig m_config;
    
    protected SnmpObjId m_sysNameOid;
    protected SnmpObjId m_ifDescr;
    protected SnmpObjId m_ifOutOctets;
    protected SnmpObjId m_invalid;
    
    protected OnmsNode m_node;
    protected OnmsIpInterface m_iface;
    
    protected SnmpCollectionAgent m_agent;
    private SnmpWalker m_walker;
    protected SnmpCollectionSet m_collectionSet;
    
    protected MockSnmpAgent m_mockAgent;
    protected IpInterfaceDao m_ifaceDao;

    protected LocationAwareSnmpClient m_locationAwareSnmpClient = new LocationAwareSnmpClientRpcImpl(new MockRpcClientFactory());

    @Override
    public void setVersion(int version) {
        super.setVersion(version);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setStartEventd(false);
        super.setUp();
        
        SnmpUtils.unsetStrategyResolver();
        System.getProperties().remove("org.opennms.snmp.strategyClass");

        m_mockAgent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties").getURL(), InetAddressUtils.str(myLocalHost()) + "/9161");
        
        m_config = new MockDataCollectionConfig();
        DataCollectionConfigFactory.setInstance(m_config);
        
        m_sysNameOid = SnmpObjId.get(".1.3.6.1.2.1.1.5");
        m_ifOutOctets = SnmpObjId.get("..1.3.6.1.2.1.2.2.1.16");
        m_invalid = SnmpObjId.get(".1.5.6.1.2.1.1.5");
        m_ifDescr = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.2");
        
        m_ifaceDao = mock(IpInterfaceDao.class);

        createAgent(1, PrimaryType.PRIMARY);
        
    }

    @After
    @Override
    public void tearDown() throws Exception {
        m_mockAgent.shutDownAndWait();

        SnmpUtils.unsetStrategyResolver();
        System.getProperties().remove("org.opennms.snmp.strategyClass");

        verify(m_ifaceDao, atLeastOnce()).load(anyInt());
        verifyNoMoreInteractions(m_ifaceDao);

        super.tearDown();
    }
    
    protected static void assertMibObjectsPresent(CollectionResource resource, final List<MibObject> attrList) {
        assertNotNull(resource);
        
        AttributeVerifier attributeVerifier = new AttributeVerifier(attrList);
		resource.visit(attributeVerifier);
		assertEquals("Unexpected number of attributes", attrList.size(), attributeVerifier.attributeCount);
    }

    protected static void assertMibObjectPresent(SnmpAttribute attribute, List<MibObject> attrList) {
        for (Iterator<MibObject> it = attrList.iterator(); it.hasNext();) {
            MibObject mibObj = it.next();
            if (mibObj.getOid().equals(((SnmpAttributeType)attribute.getAttributeType()).getOid()))
                return;
        }
        fail("Unable to find attribue "+attribute+" in attribute list");
    }

    protected void addIfNumber() {
        addAttribute("ifNumber",    ".1.3.6.1.2.1.2.1", "0", "integer");
    }

    protected void addSystemGroup() {
        addSysDescr();
        addSysOid();
//        addSysContact();
        addSysName();
        addSysLocation();
    }

    protected void addSysLocation() {
        addAttribute("sysLocation", ".1.3.6.1.2.1.1.6", "0", "string");
    }

    protected void addSysName() {
        addAttribute("sysName",     ".1.3.6.1.2.1.1.5", "0", "string");
    }

    protected void addSysContact() {
        addAttribute("sysContact",  ".1.3.6.1.2.1.1.4", "0", "string");
    }

    protected void addSysUptime() {
        addAttribute("sysUptime",   ".1.3.6.1.2.1.1.3", "0", "timeTicks");
    }

    protected void addSysOid() {
        addAttribute("sysOid",      ".1.3.6.1.2.1.1.2", "0", "string");
    }

    protected void addSysDescr() {
        addAttribute("sysDescr", ".1.3.6.1.2.1.1.1", "0", "string");
    }

    protected void addAttribute(String alias, String oid, String inst, String type) {
        m_config.addAttributeType(alias, oid, inst, type);
    }

    protected void addIfTable() {
        addIfSpeed();
        addIfInOctets();
        addIfOutOctets();
        addIfInErrors();
        addIfOutErrors();
        addIfInDiscards();
    }
    
    protected void addIpAddrTable() {
        addIpAdEntAddr();
        addIpAdEntIfIndex();
        addIpAdEntNetMask();
        addIpAdEntBcastAddr();
    }
    
    protected void addInvalid() {
        addAttribute("invalid", ".1.5.6.1.2.1.4.20.1.4", "ifIndex", "counter");
        
    }
    
    

    protected void addIpAdEntBcastAddr() {
        // .1.3.6.1.2.1.4.20.1.4
        // FIXME: be better about non specific instances.. They are not all ifIndex but we are using that to mean a column
        addAttribute("addIpAdEntBcastAddr", ".1.3.6.1.2.1.4.20.1.4", "ifIndex", "ipAddress");
    }

    protected void addIpAdEntNetMask() {
        // .1.3.6.1.2.1.4.20.1.3
        addAttribute("addIpAdEntNetMask", ".1.3.6.1.2.1.4.20.1.3", "ifIndex", "ipAddress");
        
    }

    protected void addIpAdEntIfIndex() {
        // .1.3.6.1.2.1.4.20.1.2
        addAttribute("addIpAdEntIfIndex", ".1.3.6.1.2.1.4.20.1.2", "ifIndex", "integer");
        
    }

    protected void addIpAdEntAddr() {
        // .1.3.6.1.2.1.4.20.1.1
        addAttribute("addIpAdEntAddr", ".1.3.6.1.2.1.4.20.1.1", "ifIndex", "ipAddress");
        
    }

    protected void addIfInDiscards() {
        addAttribute("ifInDiscards", ".1.3.6.1.2.1.2.2.1.13", "ifIndex", "counter");
    }

    protected void addIfOutErrors() {
        addAttribute("ifOutErrors", ".1.3.6.1.2.1.2.2.1.20", "ifIndex", "counter");
    }

    protected void addIfInErrors() {
        addAttribute("ifInErrors", ".1.3.6.1.2.1.2.2.1.14", "ifIndex", "counter");
    }

    protected void addIfOutOctets() {
        addAttribute("ifOutOctets", ".1.3.6.1.2.1.2.2.1.16", "ifIndex", "counter");
    }

    protected void addIfInOctets() {
        addAttribute("ifInOctets", ".1.3.6.1.2.1.2.2.1.10", "ifIndex", "counter");
    }

    protected void addIfSpeed() {
        addAttribute("ifSpeed", ".1.3.6.1.2.1.2.2.1.5", "ifIndex", "gauge");
    }
    
    //@Override
    //public void testDoNothing() {}

    public List<MibObject> getAttributeList() {
        return m_config.getAttrList();
    }

    protected void createAgent(int ifIndex, PrimaryType ifCollType) {
        m_node = new OnmsNode();
        m_node.setSysObjectId(".1.2.3.4.5.6.7");
        
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(m_node, ifIndex);
    
    	m_iface = new OnmsIpInterface();
        m_iface.setId(123);
        m_iface.setIpAddress(myLocalHost());
    	m_iface.setIsSnmpPrimary(ifCollType);
    	m_iface.setSnmpInterface(snmpIface);
    	m_node.addIpInterface(m_iface);
        

    	when(m_ifaceDao.load(m_iface.getId())).thenReturn(m_iface);

        m_agent = DefaultSnmpCollectionAgent.create(m_iface.getId(), m_ifaceDao, new MockPlatformTransactionManager());
        
    }
    
    protected void initializeAgent() throws CollectionInitializationException {
        ServiceParameters params = new ServiceParameters(new HashMap<String, Object>());
        OnmsSnmpCollection snmpCollection = new OnmsSnmpCollection(m_agent, params, m_config, m_locationAwareSnmpClient);
        m_collectionSet = snmpCollection.createCollectionSet(m_agent);
        m_agent.validateAgent();
    }
    
    protected SnmpCollectionSet getCollectionSet() {
        return m_collectionSet;
    }

    protected void createWalker(CollectionTracker collector) {
        m_walker = SnmpUtils.createWalker(m_agent.getAgentConfig(), getClass().getSimpleName(), collector);
        m_walker.start();
    }

    protected void waitForSignal() throws InterruptedException {
        try {
            m_walker.waitFor();
        } finally {
            m_walker.close();
        }
    }
}
