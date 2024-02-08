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
package org.opennms.netmgt.measurements.filters.impl;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.model.FilterDef;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class TrendLineIT extends AnalyticsFilterTest {
    @Test
    public void canTrend() throws Exception {
        FilterDef filterDef = new FilterDef("Trend",
                "outputColumn", "Z",
                "inputColumn", "Y",
                "secondsAhead", "1",
                "polynomialOrder", "5");

        // Use constant values for the Y column
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 100; i++) {
            table.put(i, Filter.TIMESTAMP_COLUMN_NAME, (double)(i* 1000));
            table.put(i, "Y", 1.0d);
        }

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // The Z column should be constant
        for (long i = 1; i <= 100; i++) {
            Assert.assertEquals((double) i * 1000, table.get(i, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
            Assert.assertEquals(1.0d, table.get(i, "Z"), 0.0001);
        }
    }
}
