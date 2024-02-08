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
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %><%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java" contentType="text/html" session="true" %>

    <%
        String viewName = "";

        if (request.getParameterMap().containsKey("viewName")) {
            viewName = "&viewName=" + request.getParameter("viewName");
        }
    %>

    <script type="text/javascript">

        var isInitialized = false;
        var checkInterval = setInterval(checkIframe, 1000);

        function checkIframe(){

            var iframe = document.getElementById("surveillance-iframe");

            iframe.contentWindow.postMessage("test", window.location.origin);
            if(isInitialized){
                clearInterval(checkInterval);
            }
        }

        function receiveMessage(event){
            isInitialized = true;
            if(event.origin !== window.location.origin)
                return;

            var elem = document.getElementById("surveillance-view");
            elem.style.height = event.data;
        }

        window.addEventListener("message", receiveMessage, false);

    </script>

    <div id="surveillance-view">
    <iframe name="surveillance-view-embedded" id="surveillance-iframe" src="vaadin-surveillance-views?dashboard=false<%= WebSecurityUtils.sanitizeString(viewName) %>" frameborder="0" style="min-height:100%; min-width:100%;"></iframe>
    </div>
