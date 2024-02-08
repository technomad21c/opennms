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
package org.opennms.netmgt.poller.monitors;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Check for Log matches via UCD-SNMP-MIB .
 * </p>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author <A HREF="mailto:cliles@capario.com">Chris Liles</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</a>
 * @version $Id: $
 */
final public class LogMatchTableMonitor extends SnmpMonitorStrategy {
    public static final Logger LOG = LoggerFactory.getLogger(LogMatchTableMonitor.class);

    private static final String lmTableErrorFlag = "1.3.6.1.4.1.2021.16.2.1.100";
    private static final String lmTableFileName = "1.3.6.1.4.1.2021.16.2.1.3";
    private static final String lmTableRegEx = "1.3.6.1.4.1.2021.16.2.1.4";
    private static final String lmTableCount = "1.3.6.1.4.1.2021.16.2.1.10";

    /**
     * {@inheritDoc}
     *
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = PollStatus.available();
        InetAddress ipaddr = svc.getAddress();

        ArrayList<String> errorStringReturn = new ArrayList<>();

        // Retrieve this interface's SNMP peer object
        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(ipaddr);
        LOG.debug("poll: setting SNMP peer attribute for interface {}", hostAddress);

        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        LOG.debug("poll: service= SNMP address= {}", agentConfig);

        try {
            LOG.debug("PrTableMonitor.poll: SnmpAgentConfig address: {}", agentConfig);
            SnmpObjId lmTableErrorSnmpObject = SnmpObjId.get(lmTableErrorFlag);

            Map<SnmpInstId, SnmpValue> flagResults = SnmpUtils.getOidValues(agentConfig, "LogMatchTableMonitor", lmTableErrorSnmpObject);

            if(flagResults.size() == 0) {
                LOG.debug("SNMP poll failed: no results, addr={} oid={}", hostAddress, lmTableErrorSnmpObject);
                return PollStatus.unavailable();
            }

            for (Map.Entry<SnmpInstId, SnmpValue> e : flagResults.entrySet()) { 
                LOG.debug("poll: SNMPwalk poll succeeded, addr={} oid={} instance={} value={}", hostAddress, lmTableErrorSnmpObject, e.getKey(), e.getValue());

                if (e.getValue().toString().equals("1")) {
                    LOG.debug("LogMatchTableMonitor.poll: found errorFlag=1");

                    SnmpObjId lmTableFilenameSnmpObject = SnmpObjId.get(lmTableFileName + "." + e.getKey().toString());
                    SnmpObjId lmTableRegExSnmpObject = SnmpObjId.get(lmTableRegEx + "." + e.getKey().toString());
                    SnmpObjId lmTableCountSnmpObject = SnmpObjId.get(lmTableCount + "." + e.getKey().toString());

                    String lmErrorMsg = "Rexeg " + SnmpUtils.get(agentConfig,lmTableRegExSnmpObject).toDisplayString() + ", for log file " + SnmpUtils.get(agentConfig,lmTableFilenameSnmpObject).toDisplayString() + " has matched " + SnmpUtils.get(agentConfig,lmTableCountSnmpObject).toDisplayString() + "time(s).";

                    //Stash the error in an ArrayList to then enumerate over later
                    errorStringReturn.add(lmErrorMsg);
                }
            }

            //Check the arraylist and construct return value
            if (errorStringReturn.size() > 0) {
                return PollStatus.unavailable(errorStringReturn.toString());
            }
            else {
                return status;
            }

        } catch (NumberFormatException e) {
            String reason1 = "Number operator used on a non-number " + e.getMessage();
            LOG.error(reason1, e);
            return PollStatus.unavailable(reason1);
        } catch (IllegalArgumentException e) {
            String reason1 = "Invalid SNMP Criteria: " + e.getMessage();
            LOG.error(reason1, e);
            return PollStatus.unavailable(reason1);
        } catch (Throwable t) {
            String reason1 = "Unexpected exception during SNMP poll of interface " + hostAddress;
            LOG.warn(reason1, t);
            return PollStatus.unavailable(reason1);
        }

    }

}
