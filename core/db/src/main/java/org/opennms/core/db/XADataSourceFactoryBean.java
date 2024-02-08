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
package org.opennms.core.db;


import javax.sql.XADataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

/**
 * <p>XADataSourceFactoryBean class.</p>
 */
public class XADataSourceFactoryBean implements FactoryBean<XADataSource>, DisposableBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(XADataSourceFactoryBean.class);

    /**
     * <p>getObject</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public XADataSource getObject() throws Exception {
        return XADataSourceFactory.getXADataSource();
    }

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    @Override
    public Class<? extends XADataSource> getObjectType() {
        return (XADataSourceFactory.getXADataSource() == null ? XADataSource.class : XADataSourceFactory.getXADataSource().getClass());
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void destroy() throws Exception {
        LOG.info("Closing {}!!!", getClass().getSimpleName());
        XADataSourceFactory.close();
    }

}
