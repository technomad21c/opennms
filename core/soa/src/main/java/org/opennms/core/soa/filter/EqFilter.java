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
package org.opennms.core.soa.filter;


/**
 * EqFilter
 *
 * @author brozow
 */
public class EqFilter extends AttributeComparisonFilter {
    
    private String m_value;

    public EqFilter(String attribute, String value) {
        super(attribute);
        m_value = value;
    }
    
    

    @Override
    protected boolean valueMatches(String value) {
        return m_value.equals(value);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("(");
        buf.append(getAttribute());
        buf.append("=");
        buf.append(escaped(m_value));
        buf.append(")");
        return buf.toString();
        
    }
    
    private String escaped(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("*", "\\*")
            .replace(")", "\\)")
            .replace("(", "\\(")
            ;
    }

}
