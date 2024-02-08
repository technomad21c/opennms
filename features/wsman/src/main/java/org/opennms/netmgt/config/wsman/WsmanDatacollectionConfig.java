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
package org.opennms.netmgt.config.wsman;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wsman-datacollection}collection" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wsman-datacollection}group" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wsman-datacollection}system-definition" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="rrdRepository" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "collection",
    "group",
    "systemDefinition"
})
@XmlRootElement(name = "wsman-datacollection-config")
public class WsmanDatacollectionConfig {

    @XmlElement(required = true)
    protected List<Collection> collection;
    @XmlElement(required = true)
    protected List<Group> group;
    @XmlElement(name = "system-definition", required = true)
    protected List<SystemDefinition> systemDefinition;
    @XmlAttribute(name = "rrd-repository")
    protected String rrdRepository;

    /**
     * Gets the value of the collection property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the collection property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCollection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Collection }
     * 
     * 
     */
    public List<Collection> getCollection() {
        if (collection == null) {
            collection = new ArrayList<>();
        }
        return this.collection;
    }

    public void addCollection(org.opennms.netmgt.config.wsman.Collection collection) {
        getCollection().add(Objects.requireNonNull(collection));
    }

    /**
     * Gets the value of the group property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the group property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Group }
     * 
     * 
     */
    public List<Group> getGroup() {
        if (group == null) {
            group = new ArrayList<>();
        }
        return this.group;
    }

    public void addGroup(Group group) {
        getGroup().add(Objects.requireNonNull(group));
    }

    /**
     * Gets the value of the systemDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the systemDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSystemDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SystemDefinition }
     * 
     * 
     */
    public List<SystemDefinition> getSystemDefinition() {
        if (systemDefinition == null) {
            systemDefinition = new ArrayList<>();
        }
        return this.systemDefinition;
    }

    public void addSystemDefinition(SystemDefinition sysDef) {
        getSystemDefinition().add(Objects.requireNonNull(sysDef));
    }

    /**
     * Gets the value of the rrdRepository property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRrdRepository() {
        return rrdRepository;
    }

    /**
     * Sets the value of the rrdRepository property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRrdRepository(String value) {
        this.rrdRepository = value;
    }

    public WsmanDatacollectionConfig merge(WsmanDatacollectionConfig other) {
        if (other == null) {
            return this;
        }
        // Overwrite the rrdRepository iff it's null
        if (rrdRepository == null && other.rrdRepository != null) {
            rrdRepository = other.rrdRepository;
        }
        // Merge the lists of collections, groups, and system definitions
        getCollection().addAll(other.getCollection());
        getGroup().addAll(other.getGroup());
        getSystemDefinition().addAll(other.getSystemDefinition());
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection, group, systemDefinition, rrdRepository);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WsmanDatacollectionConfig other = (WsmanDatacollectionConfig) obj;
        return Objects.equals(this.collection, other.collection) &&
                Objects.equals(this.group, other.group) &&
                Objects.equals(this.systemDefinition, other.systemDefinition) &&
                Objects.equals(this.rrdRepository, other.rrdRepository);
    }
}
