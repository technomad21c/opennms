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
package org.opennms.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.opennms.core.logging.Logging;
import org.opennms.core.resource.Vault;

/**
 * Encapsulates all initialization and configuration needed by the OpenNMS
 * servlets and JSPs.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public abstract class ServletInitializer {
    /**
     * Private, empty constructor so that this class cannot be instantiated
     * outside of itself.
     */
    private ServletInitializer() {
    }

    /**
     * Initialize servlet and JSP configuration on the first invocation of this
     * method. All other invocations are ignored. This method is synchronized to
     * ensure only the first invocation performs the initialization.
     *
     * <p>
     * Call this method in the <code>init</code> method of your servlet or
     * JSP. It will read the servlet initialization parameters from the
     * <code>ServletConfig</code> and <code>ServletContext</code> and
     * OpenNMS configuration files.
     * </p>
     *
     * @param context
     *            the <code>ServletContext</code> instance in which your
     *            servlet is running
     * @throws javax.servlet.ServletException if any.
     */
    public static synchronized void init(ServletContext context) throws ServletException {
        if (context == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        /*
         * All ThreadCategory instances in the WebUI should use this as their
         * category prefix
         */
        
        Logging.putPrefix("web");

        Properties properties = new Properties();
        properties.putAll(System.getProperties());

        /*
         * First, check if opennms.home is set, if so, we already have properties
         * because we're in Jetty.
         */
        if (properties.getProperty("opennms.home") == null) {
            throw new ServletException("The opennms.home context parameter must be set.");
        }

        String homeDir = properties.getProperty("opennms.home");

        /*
         * Now that we've got opennms.home, load $OPENNMS_HOME/etc/opennms.properties
         * in case it isn't--but if anything is already set, we don't override it.
         */
        Properties opennmsProperties = new Properties();

        try {
        	loadPropertiesFromFile(opennmsProperties, homeDir + File.separator + "etc" + File.separator + "opennms.properties");
        } catch (IOException e) {
        	throw new ServletException("Could not load opennms.properties", e);
        }

        try {
        	loadPropertiesFromContextResource(context, opennmsProperties, "/WEB-INF/version.properties");
        } catch (IOException e) {
        	throw new ServletException("Could not load version.properties", e);
        }

        for (Object key : opennmsProperties.keySet()) {
        	if (!properties.containsKey(key)) {
        		properties.put(key, opennmsProperties.get(key));
        	}
        }

        for (String name : Collections.list(context.getInitParameterNames())) {
        	properties.put(name, context.getInitParameter(name));
        }

        Vault.setProperties(properties);
    }

    private static void loadPropertiesFromFile(Properties opennmsProperties, String propertiesFile) throws FileNotFoundException, ServletException, IOException {
        InputStream configurationStream = new FileInputStream(propertiesFile);
        opennmsProperties.load(configurationStream);
        configurationStream.close();
    }

    private static void loadPropertiesFromContextResource(ServletContext context, Properties properties, String propertiesResource) throws ServletException, IOException {
        InputStream configurationStream = context.getResourceAsStream(propertiesResource);
        if (configurationStream == null) {
            throw new ServletException("Could not load properties from resource '" + propertiesResource + "'");
        } else {
            properties.load(configurationStream);
            configurationStream.close();
        }
    }
}
