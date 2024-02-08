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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.02.13 at 11:10:03 AM EST 
//


package org.opennms.netmgt.xml.rtc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="catlabel" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="catvalue" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/rtceui}node" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "catlabel",
    "catvalue",
    "node"
})
@XmlRootElement(name = "category")
public class Category {

    @XmlElement(required = true)
    protected String catlabel;
    protected double catvalue;
    protected List<Node> node;

    /**
     * Gets the value of the catlabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCatlabel() {
        return catlabel;
    }

    /**
     * Sets the value of the catlabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCatlabel(String value) {
        this.catlabel = value;
    }

    /**
     * Gets the value of the catvalue property.
     * 
     */
    public double getCatvalue() {
        return catvalue;
    }

    /**
     * Sets the value of the catvalue property.
     * 
     */
    public void setCatvalue(double value) {
        this.catvalue = value;
    }

    /**
     * A node that belongs to this
     *             category Gets the value of the node property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the node property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Node }
     * 
     * 
     */
    public List<Node> getNode() {
        if (node == null) {
            node = new ArrayList<>();
        }
        return this.node;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Category)) {
            return false;
        }
        Category castOther = (Category) other;
        return Objects.equals(catlabel, castOther.catlabel) && Objects.equals(catvalue, castOther.catvalue)
                && Objects.equals(node, castOther.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catlabel, catvalue, node);
    }

}
