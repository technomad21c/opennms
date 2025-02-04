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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;

import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

/**
 * From RFC6368:
 *
 * The attribute value consists of a 4-octet "Origin AS" value followed
 * by a variable-length field that conforms to the BGP UPDATE message
 * path attribute encoding rules.  The length of this attribute is 4
 * plus the total length of the encoded attributes.
 *
 *                       +------------------------------+
 *                       | Origin AS (4 octets)         |
 *                       +------------------------------+
 *                       | Path Attributes (variable)   |
 *                       +------------------------------+
 *
 */
public class AttrSet implements Attribute {
    public final long originAs;
    public final List<UpdatePacket.PathAttribute> pathAttributes;

    public AttrSet(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        this.originAs = uint32(buffer);
        this.pathAttributes = repeatRemaining(buffer, pathAttributeBuffer -> new UpdatePacket.PathAttribute(pathAttributeBuffer, flags, peerInfo));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("originAs", this.originAs)
                .add("pathAttributes", this.pathAttributes)
                .toString();
    }
}
