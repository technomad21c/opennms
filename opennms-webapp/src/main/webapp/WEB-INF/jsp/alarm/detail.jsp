<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.util.*,
        org.opennms.core.resource.Vault,
        org.opennms.core.utils.InetAddressUtils,
        org.opennms.core.utils.WebSecurityUtils,
        org.opennms.web.controller.alarm.*,
        org.opennms.web.alarm.*,
        org.opennms.web.servlet.XssRequestWrapper,
        org.opennms.netmgt.model.OnmsAcknowledgment,
        org.opennms.netmgt.model.OnmsAlarm,
        org.opennms.netmgt.model.OnmsSeverity,
        org.opennms.netmgt.model.TroubleTicketState,
        org.opennms.web.api.Authentication,
        org.opennms.web.api.Util,
        org.apache.commons.configuration.Configuration,
        org.apache.commons.configuration.ConfigurationException,
        org.apache.commons.configuration.PropertiesConfiguration,
        org.opennms.web.element.NetworkElementFactory,
        java.util.stream.Collectors"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/taglib.tld" prefix="onms" %>
<%@taglib tagdir="/WEB-INF/tags/form" prefix="form" %>

<%!
    public String alarmTicketLink(OnmsAlarm alarm) {
        String template = System.getProperty("opennms.alarmTroubleTicketLinkTemplate");
        if (template == null) {
            return alarm.getTTicketId();
        } else {
            template = template.replaceAll("\\$\\{id\\}", alarm.getTTicketId());
            return "<a href=\"" + template + "\">" + alarm.getTTicketId() + "</a>";
        }
    }

%>

<%
    final Comparator<OnmsAlarm> SORT_ALARMS_BY_ID = new Comparator<OnmsAlarm>() {
        public int compare(final OnmsAlarm o1, final OnmsAlarm o2) {
            return Integer.compare(o1.getId(), o2.getId());
        }
    };

    XssRequestWrapper req = new XssRequestWrapper(request);
    OnmsAlarm alarm = (OnmsAlarm) request.getAttribute("alarm");
    final String alarmId = (String)request.getAttribute("alarmId");

    if (alarm == null) {
        throw new AlarmIdNotFoundException("Missing alarm request attribute.", alarmId);
    }

    pageContext.setAttribute("alarm", alarm);

    String action = null;
    String ackButtonName = null;
    String ackButtonIcon = null;
    boolean showEscalate = false;
    boolean showClear = false;

    if (alarm.getAckTime() == null) {
        ackButtonName = "Acknowledge";
        action = AcknowledgeType.ACKNOWLEDGED.getShortName();
        ackButtonIcon = "fa fa-check-square-o";
    } else {
        ackButtonName = "Unacknowledge";
        action = AcknowledgeType.UNACKNOWLEDGED.getShortName();
        ackButtonIcon = "fa fa-square-o";
    }

    String escalateAction = AlarmSeverityChangeController.ESCALATE_ACTION;
    String clearAction = AlarmSeverityChangeController.CLEAR_ACTION;
    if (alarm.getSeverity() == OnmsSeverity.CLEARED || (alarm.getSeverity().isGreaterThan(OnmsSeverity.NORMAL) && alarm.getSeverity().isLessThan(OnmsSeverity.CRITICAL))) {
        showEscalate = true;
    }
    if (alarm.getSeverity().isGreaterThanOrEqual(OnmsSeverity.NORMAL) && alarm.getSeverity().isLessThanOrEqual(OnmsSeverity.CRITICAL)) {
        showClear = true;
    }
    
    List<OnmsAcknowledgment> acks = (List<OnmsAcknowledgment>) request.getAttribute("acknowledgments");

	String eventLocation = null;
	String nodeLocation = null;

	if (alarm.getLastEvent() != null && alarm.getLastEvent().getDistPoller() != null) {
	    eventLocation = alarm.getLastEvent().getDistPoller().getLocation();
	}
	if (alarm.getNode() != null && alarm.getNode().getLocation() != null) {
	    nodeLocation = alarm.getNode().getLocation().getLocationName();
	}
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Detail")
          .headTitle("Alarms")
          .breadcrumb("Alarms", "alarm/index.htm")
          .breadcrumb((alarm.isSituation() ? "Situation " : "Alarm ") + alarm.getId())
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
    let url = new URL(location.href);
    if (!url.searchParams.has('id')) {
        url.searchParams.set('id', '${alarmId}');
        window.location.href = url.href;
    }
</script>

<div class="card">
  <div class="card-header">
    <span><%= (alarm.isSituation() ? "Situation " : "Alarm ") + alarm.getId()%></span>
  </div>

<table class="table table-sm severity">
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">Severity</th>
        <td class="col-4 bright"><%=alarm.getSeverity().getLabel()%></td>
        <th class="col-2">Node</th>
        <td class="col-4">
            <% if (alarm.getNodeId() != null && alarm.getNodeId() > 0) {%>
            <c:url var="nodeLink" value="element/node.jsp">
                <c:param name="node" value="<%=String.valueOf(alarm.getNodeId())%>"/>
            </c:url>
            <a href="${nodeLink}"><c:out value="<%=alarm.getNodeLabel()%>"/></a>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">Last&nbsp;Event</th>
        <td class="col-4"><span title="Event <%= alarm.getLastEvent().getId()%>"><a href="event/detail.jsp?id=<%= alarm.getLastEvent().getId()%>"><onms:datetime date="<%=alarm.getLastEventTime()%>" /></a></span></td>
        <th class="col-2">Interface</th>
        <td class="col-4">
            <% if (alarm.getIpAddr() != null) {%>
            <% if (alarm.getNodeId() != null && alarm.getNodeId() > 0) {%>
            <c:url var="interfaceLink" value="element/interface.jsp">
                <c:param name="node" value="<%=String.valueOf(alarm.getNodeId())%>"/>
                <c:param name="intf" value="<%=InetAddressUtils.str(alarm.getIpAddr())%>"/>
            </c:url>
            <a href="${interfaceLink}"><%=InetAddressUtils.str(alarm.getIpAddr())%></a>
            <% } else {%>
            <%=InetAddressUtils.str(alarm.getIpAddr())%>
            <% }%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">First&nbsp;Event</th>
        <td class="col-4"><onms:datetime date="<%=alarm.getFirstEventTime()%>" /></td>
        <th class="col-2">Service</th>
        <td class="col-4">
            <% if (alarm.getServiceType() != null) {%>
            <% if (alarm.getIpAddr() != null && alarm.getNodeId() > 0) {%>
            <c:url var="serviceLink" value="element/service.jsp">
                <c:param name="node" value="<%=String.valueOf(alarm.getNodeId())%>"/>
                <c:param name="intf" value="<%=InetAddressUtils.str(alarm.getIpAddr())%>"/>
                <c:param name="service" value="<%=String.valueOf(alarm.getServiceType().getId())%>"/>
            </c:url>
            <a href="${serviceLink}"><c:out value="<%=alarm.getServiceType().getName()%>"/></a>
            <% } else {%>
            <c:out value="<%=alarm.getServiceType().getName()%>"/>
            <% }%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr> 
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">Event Source Location</th>
        <td class="col-4"><%= eventLocation == null? "&nbsp;" : eventLocation %>
        <th class="col-2">Node Location</th>
        <td class="col-4"><%= nodeLocation == null? "&nbsp;" : nodeLocation %>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">Count</th>
        <td class="col-4"><%=alarm.getCounter()%></td>
        <th class="col-2">UEI</th>
        <td class="col-4">
            <% if (alarm.getUei() != null) {%>
            <%=alarm.getUei()%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">Managed Object Type</th>
        <td class="col-4">
            <% if (alarm.getManagedObjectType() != null) {%>
            <%=alarm.getManagedObjectType()%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
        <th class="col-2">Managed Object Instance</th>
        <td class="col-4">
            <% if (alarm.getManagedObjectInstance() != null) {%>
            <%=alarm.getManagedObjectInstance()%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">Ticket&nbsp;ID</th>
        <td class="col-4"><% if (alarm.getTTicketId() == null) {%>
            &nbsp;
            <% } else {%>
            <%= alarmTicketLink(alarm)%> 
            <% }%>
        </td>
        <th class="col-2">Ticket&nbsp;State</th>
        <td class="col-4"><% if (alarm.getTTicketState() == null) {%>
            &nbsp;
            <% } else {%>
            <%= alarm.getTTicketState()%> 
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">Reduction&nbsp;Key</th>
        <td class="col-10" colspan="3">
            <% if (alarm.getReductionKey() != null) {%>
            <%=WebSecurityUtils.sanitizeString(alarm.getReductionKey())%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%> d-flex">
        <th class="col-2">Resolvable</th>
        <td class="col-10" colspan="3">
            <%
                final Set<String> resolvingUeis = NetworkElementFactory.getInstance(getServletContext()).getResolvingUeisForAlarmUei(alarm.getUei());
                if (resolvingUeis.size() == 0) {
            %>No<% } else {%>Yes, by
            <%=resolvingUeis.stream().collect(Collectors.joining(", "))%>
            <% }%>
        </td>
    </tr>
</table>
</div>

<div class="card severity">
  <div class="card-header">
    <span>Log&nbsp;Message</span>
  </div>
  <div class="card-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
    <%=WebSecurityUtils.sanitizeString(alarm.getLogMsg(), true)%>
  </div>
</div>

<div class="card severity">
  <div class="card-header">
    <span>Description</span>
  </div>
  <div class="card-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
    <%=WebSecurityUtils.sanitizeString(alarm.getDescription(), true)%>
  </div>
</div>

<% if (alarm.isPartOfSituation()) { %>
<div class="card">
    <div class="card-header">
        <span>Parent Situation(s)</span>
    </div>
    <table class="table table-sm severity">
        <thead>
        <tr>
            <th width="2%">ID</th>
            <th width="6%">Severity</th>
            <th width="20%">Node</th>
            <th width="4%">Count</th>
            <th width="14%">Last</th>
            <th width="54%">Log Msg</th>
        </tr>
        </thead>
        <%
            final TreeSet<OnmsAlarm> sortedSet = new TreeSet<>(SORT_ALARMS_BY_ID);

            sortedSet.addAll(alarm.getRelatedSituations());
            pageContext.setAttribute("sortedSet", sortedSet);
        %>
        <c:forEach var="relatedVar" items="${alarm.relatedSituations}">
            <tr class="severity-${relatedVar.severityLabel.toLowerCase()}">
                <td class="divider" valign="middle">
                    <a style="vertical-align:middle" href="<%= Util.calculateUrlBase(request, "alarm/detail.htm?id=") %>${relatedVar.id}">${relatedVar.id}</a>
                </td>
                <td class="divider bright" valign="middle">
                    <nobr>
                        <strong>${relatedVar.severityLabel}</strong>
                    </nobr>
                </td>
                <td class="divider" valign="middle">
                    <a href="element/node.jsp?node=${relatedVar.nodeId}">${relatedVar.nodeLabel}</a>
                </td>
                <td class="divider" valign="middle">
                        ${relatedVar.counter}
                </td>
                <td class="divider" valign="middle">
                    <c:if test="${relatedVar.lastEvent != null }">
	                        <span title="Event ${relatedVar.lastEvent.id}">
	                            <a href="event/detail.htm?id=${relatedVar.lastEvent.id}">
	                                <onms:datetime date="${relatedVar.lastEventTime}" />
	                            </a>
	                        </span>
                    </c:if>
                </td>
                <td class="divider" valign="middle">
                        ${relatedVar.logMsg}
                </td>
            </tr>
        </c:forEach>
    </table>
</div>
<% } %>

<% if (alarm.isSituation()) { %>
<div class="card">
    <div class="card-header">
        <span>Related Alarm(s)</span>
    </div>
    <table class="table table-sm severity">
        <thead>
        <tr>
            <th width="2%">ID</th>
            <th width="4%">Situation</th>
            <th width="6%">Severity</th>
            <th width="20%">Node</th>
            <th width="4%">Count</th>
            <th width="14%">Last</th>
            <th width="50%">Log Msg</th>
        </tr>
        </thead>
        <%
            final TreeSet<OnmsAlarm> sortedSet = new TreeSet<>(SORT_ALARMS_BY_ID);

            sortedSet.addAll(alarm.getRelatedAlarms());
            pageContext.setAttribute("sortedSet", sortedSet);
        %>
        <c:forEach var="relatedVar" items="${sortedSet}">
            <tr class="severity-${relatedVar.severityLabel.toLowerCase()}">
                <td class="divider" valign="middle">
                    <a style="vertical-align:middle" href="<%= Util.calculateUrlBase(request, "alarm/detail.htm?id=") %>${relatedVar.id}">${relatedVar.id}</a>
                </td>
                <td>
                    <c:choose>
                    <c:when test="${relatedVar.situation}">
                    <i class="fa fa-check-square-o">
                        </c:when>
                        <c:otherwise>
                        <i class="fa fa-square-o">
                            </c:otherwise>
                            </c:choose>
                </td>
                <td class="divider bright" valign="middle">
                    <nobr>
                        <strong>${relatedVar.severityLabel}</strong>
                    </nobr>
                </td>
                <td class="divider" valign="middle">
                    <a href="element/node.jsp?node=${relatedVar.nodeId}">${relatedVar.nodeLabel}</a>
                </td>
                <td class="divider" valign="middle">
                        ${relatedVar.counter}
                </td>
                <td class="divider" valign="middle">
                    <c:if test="${relatedVar.lastEvent != null }">
	                        <span title="Event ${relatedVar.lastEvent.id}">
	                            <a href="event/detail.htm?id=${relatedVar.lastEvent.id}">
	                                <onms:datetime date="${relatedVar.lastEventTime}" />
	                            </a>
	                        </span>
                    </c:if>
                </td>
                <td class="divider" valign="middle">
                        ${relatedVar.logMsg}
                </td>
            </tr>
        </c:forEach>
    </table>
</div>
<% } %>

<div class="card">
    <div class="card-header">
        <span>Related Events</span>
    </div>
    <%
        final List<RelatedEvent> related = (List<RelatedEvent>)request.getAttribute("related");
        if (related.size() > 0) {
    %>
    <table class="table table-sm severity">
        <thead>
            <tr>
                <th class="divider" width="50em">Event</th>
                <th class="divider" width="100em">Severity</th>
                <th width="200em">Time</th>
                <th class="divider">UEI</th>
            </tr>
        </thead>
        <tbody>
            <% for (final RelatedEvent entry : related) { %>
                <tr class="severity-<%=entry.getSeverity().getLabel().toLowerCase()%>">
                    <td rowspan="2">
                        <% if (entry.getEventId() != 0) { %>
                            <a href="event/detail.jsp?id=<%= entry.getEventId() %>"><%= entry.getEventId() %></a>
                        <% } %>
                    </td>
                    <td rowspan="2"><%= entry.getSeverity().getLabel() %></td>
                    <td><onms:datetime date="<%= entry.getCreationTime() %>" /></td>
                    <td style="width: auto"><%= entry.getUei() %></td>
                </tr>
                <tr class="severity-<%=entry.getSeverity().getLabel().toLowerCase()%>">
                    <td colspan="2"><%= entry.getLogMessage() %></td>
                </tr>
            <% } %>
        </tbody>
    </table>
    <% } %>
</div>

<% if (acks != null && acks.size() > 0) {%>
<div class="card severity">
  <div class="card-header">
    <span>Acknowledgements</span>
  </div>
<table class="table table-sm severity">
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th>Acknowledged&nbsp;By</th>
        <th>Acknowledged&nbsp;Type</th>
        <th>Time&nbsp;Acknowledged</th>
    </tr>
    <% for (OnmsAcknowledgment ack : acks) {%>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <td><%=WebSecurityUtils.sanitizeString(ack.getAckUser())%></td>
        <td><%=ack.getAckAction()%></td>
        <td><onms:datetime date="<%=ack.getAckTime()%>" /></td>
    </tr>
    <% }%>
</table>
</div>
<% }%>

<div class="row">
<div class="col-md-6">
<div class="card severity">
  <div class="card-header">
    <span>Sticky&nbsp;Memo</span>
  </div>
  <div class="card-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
	         <form class="form" method="post" action="alarm/saveStickyMemo.htm">
                 <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
				<textarea <%=request.isUserInRole(Authentication.ROLE_READONLY)?"readonly":""%> class="w-100 mb-1" name="stickyMemoBody" ><%=(alarm.getStickyMemo() != null && alarm.getStickyMemo().getBody() != null) ? alarm.getStickyMemo().getBody() : ""%></textarea>
				<input type="hidden" name="alarmId" value="<%=alarm.getId() %>"/>
                <input <%=request.isUserInRole(Authentication.ROLE_READONLY)?"disabled":""%> class="btn btn-sm btn-secondary" type="submit" value="Save" />
                <input <%=request.isUserInRole(Authentication.ROLE_READONLY)?"disabled":""%> class="btn btn-sm btn-secondary" type="button" value="Delete" onclick="document.getElementById('deleteStickyForm').submit();"/>
	         </form>
	         <form id="deleteStickyForm" method="post" action="alarm/removeStickyMemo.htm">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
				<input type="hidden" name="alarmId" value="<%=alarm.getId() %>"/>
	         </form>
	         <br/>
        <% if (alarm.getStickyMemo() != null) { %>
	         <div class="row">
        <div class="col-md-4"><strong>Author:</strong><br/><%=(alarm.getStickyMemo().getAuthor() != null) ? alarm.getStickyMemo().getAuthor() : ""%></div>
        <div class="col-md-4"><strong>Updated:</strong><br/>
       		<span style="white-space:nowrap;"><onms:datetime date="<%=alarm.getStickyMemo().getUpdated()%>" /></span>
        </div>
        <div class="col-md-4"><strong>Created:</strong><br/>
       		<span style="white-space:nowrap;"><onms:datetime date="<%=alarm.getStickyMemo().getCreated()%>"/></span>
        </div>
        </div>
        <% } else { %>
        <div class="row">
        <div class="col-md-12">&nbsp;</div>
        <div class="col-md-12">&nbsp;</div>
        </div>
        <% } %>
</div>
</div>
</div>

<div class="col-md-6">
<div class="card severity">
  <div class="card-header">
    <span>Journal&nbsp;Memo</span>
  </div>
  <div class="card-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
            <form class="form" method="post" action="alarm/saveJournalMemo.htm">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <textarea <%=request.isUserInRole(Authentication.ROLE_READONLY)?"readonly":""%> class="w-100 mb-1" name="journalMemoBody" ><%=(alarm.getReductionKeyMemo() != null && alarm.getReductionKeyMemo().getBody() != null) ? alarm.getReductionKeyMemo().getBody() : ""%></textarea>
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
                <input <%=request.isUserInRole(Authentication.ROLE_READONLY)?"disabled":""%> class="btn btn-sm btn-secondary" type="submit" value="Save"/>
                <input <%=request.isUserInRole(Authentication.ROLE_READONLY)?"disabled":""%> class="btn btn-sm btn-secondary" type="button" value="Delete" onclick="document.getElementById('deleteJournalForm').submit();"/>
            </form>
            <form id="deleteJournalForm" method="post" action="alarm/removeJournalMemo.htm">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
            </form>
	         <br/>
        <% if (alarm.getReductionKeyMemo() != null) { %>
        <div class="row">
        <div class="col-md-4"><strong>Author:</strong><br/><%=(alarm.getReductionKeyMemo().getAuthor() != null) ? alarm.getReductionKeyMemo().getAuthor() : ""%></div>
        <div class="col-md-4"><strong>Updated:</strong><br/>
       		<span style="white-space:nowrap;"><onms:datetime date="<%=alarm.getReductionKeyMemo().getUpdated()%>" /></span>
        </div>
        <div class="col-md-4"><strong>Created:</strong><br/>
       		<span style="white-space:nowrap;"><onms:datetime date="<%=alarm.getReductionKeyMemo().getCreated()%>" /></span>
        </div>
        </div>
        <% } else { %>
        <div class="row">
        <div class="col-md-12">&nbsp;</div>
        <div class="col-md-12">&nbsp;</div>
        </div>
        <% } %>

</div>
</div>
</div>

</div>

<div class="card severity">
  <div class="card-header">
    <span>Operator&nbsp;Instructions</span>
  </div>
  <div class="card-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
            <%if (alarm.getOperInstruct() == null) {%>
            No instructions available.
            <% } else {%>
            <%=alarm.getOperInstruct()%>
            <% }%>
  </div>
</div>

<% if (request.isUserInRole(Authentication.ROLE_ADMIN) || !request.isUserInRole(Authentication.ROLE_READONLY)) {%>
    <div class="row">
        <div class="col-md-6">
            <div class="input-group mt-2">
            <form class="form-inline mr-1" method="post" action="alarm/acknowledge">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <input type="hidden" name="actionCode" value="<%=action%>" />
                <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                <input type="hidden" name="redirect" value="<%= "detail.htm" + "?" + request.getQueryString()%>" />
                <button class="form-control btn btn-secondary" type="submit">
                    <i class="<%=ackButtonIcon%>"></i> <%=ackButtonName%>
                </button>
            </form>

            <%if (showEscalate) {%>
                <form class="form-inline mr-1" method="post" action="alarm/changeSeverity?actionCode=<%=escalateAction%>">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                    <input type="hidden" name="redirect" value="<%= "detail.htm" + "?" + request.getQueryString()%>" />
                    <button class="form-control btn btn-secondary" type="submit">
                        <i class="fa fa-arrow-up"></i> Escalate
                    </button>
                </form>
            <%}%>

            <%if (showClear) {%>
                <form class="form-inline" method="post" action="alarm/changeSeverity?actionCode=<%=clearAction%>">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                    <input type="hidden" name="redirect" value="<%= "detail.htm" + "?" + request.getQueryString()%>" />
                    <button class="form-control btn btn-secondary" type="submit" value="Clear">
                        <i class="fa fa-thumbs-up"></i> Clear
                    </button>
                </form>
            <%}%>
            </div>
        </div>
        <% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled"))) {%>
        <div class="col-md-6">
            <div class="input-group mt-2">
            <form class="form-inline mr-1" method="post" action="alarm/ticket/create.htm">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
                <form:input class="form-control btn btn-secondary" type="submit" value="Create Ticket" disabled="<%=((alarm.getTTicketState() != null) && (alarm.getTTicketState() != TroubleTicketState.CREATE_FAILED)) ? true : false %>" />
            </form>

            <form class="form-inline mr-1" method="post" action="alarm/ticket/update.htm">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
                <form:input class="form-control btn btn-secondary" type="submit" value="Update Ticket" disabled="<%=(alarm.getTTicketState() == null || alarm.getTTicketId() == null) %>"/>
            </form>

            <form class="form-inline" method="post" action="alarm/ticket/close.htm">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
                <form:input class="form-control btn btn-secondary" type="submit" value="Close Ticket" disabled="<%=((alarm.getTTicketState() == null) || ((alarm.getTTicketState() != TroubleTicketState.OPEN) && (alarm.getTTicketState() != TroubleTicketState.CLOSE_FAILED))) ? true : false %>" />
            </form>
            </div>
        </div>
        <% } // alarmTroubleTicketEnabled %>
    </div>
<br/>
<% } // isUserInRole %>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
