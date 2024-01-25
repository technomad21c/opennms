/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.internal.userdatacollection;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataCollectionServiceImpl implements UserDataCollectionService {
    private static final Logger LOG = LoggerFactory.getLogger(UserDataCollectionServiceImpl.class);

    private UserDataCollectionSubmissionClient client;

    /**
     * The form with user data collection info has been received; validate and send to
     * OpenNMS Stats endpoint for further processing.
     * Does not update User Data Collection status in Config Management, client will make
     * a separate call for that.
     */
    public void submit(UserDataCollectionFormData data) throws Exception, IOException {
        var submissionData = createSubmissionData(data);
        String json = jsonSerialize(submissionData);

        try {
            client.postForm(json);
        } catch (Exception e) {
            throw e;
        }
    }

    private UserDataCollectionSubmissionData createSubmissionData(UserDataCollectionFormData data) {
        var submissionData = new UserDataCollectionSubmissionData();
        submissionData.consent = true;
        submissionData.firstName = data.firstName;
        submissionData.lastName = data.lastName;
        submissionData.email = data.email;
        submissionData.company = data.company;
        submissionData.product = "Horizon";
        submissionData.systemId = "";

        return submissionData;
    }

    private String jsonSerialize(UserDataCollectionSubmissionData data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY);
        mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);

        try {
            return mapper.writeValueAsString(data);
        } catch (IOException e) {
            LOG.error("Error serializing submission Json data", e);
            throw e;
        }
    }

    public void setClient(UserDataCollectionSubmissionClient client) {
        this.client = client;
    }
}
