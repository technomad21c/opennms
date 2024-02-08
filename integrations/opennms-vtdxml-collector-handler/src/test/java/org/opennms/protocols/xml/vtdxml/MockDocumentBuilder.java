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
package org.opennms.protocols.xml.vtdxml;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * The Mock Document Builder for VTD-XML.
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
public class MockDocumentBuilder {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(MockDocumentBuilder.class);

    /** The XML file name. */
    public static String m_xmlFileName;

    /**
     * Instantiates a new mock document builder.
     */
    private MockDocumentBuilder() {}

    /**
     * Gets the XML document.
     *
     * @return the XML document
     */
    public static VTDNav getVTDXmlDocument() {
        LOG.debug("getXmlDocument: m_xmlFileName: '{}'", m_xmlFileName);
        if (m_xmlFileName == null)
            return null;
        VTDNav doc = null;
        try {
            VTDGen vg = new VTDGen();
            if(vg.parseFile(m_xmlFileName, false)) {
                LOG.debug("getXmlDocument: parsed file '{}'", m_xmlFileName);
                return vg.getNav();
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return doc;
    }

    /**
     * Sets the XML file name.
     *
     * @param xmlFileName the new XML file name
     */
    public static void setXmlFileName(String xmlFileName) {
        m_xmlFileName = xmlFileName;
    }

}

