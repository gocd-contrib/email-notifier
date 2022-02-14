/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tw.go.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FilterConverterTest {

    private FilterConverter filterConverter;

    @BeforeEach
    public void setup() {
        this.filterConverter = new FilterConverter();
    }

    @Test
    public void testSingleFilterConverted() {
        String settingInput = "TempTestBaseInf:cloudformation:building";

        List<Filter> filterList = filterConverter.convertStringToFilterList(settingInput);

        assertNotNull(filterList);
        assertEquals(1, filterList.size());

        Filter expectedFilter = new Filter("TempTestBaseInf", "cloudformation", "building");

        assertEquals(expectedFilter, filterList.get(0));
    }

    @Test
    public void testMultipleFiltersConverted() {
        String settingInput = "TempTestBaseInf:cloudformation:building,SmokeTestNetworkInf:ansible:failed";

        List<Filter> filterList = filterConverter.convertStringToFilterList(settingInput);

        assertNotNull(filterList);
        assertEquals(2, filterList.size());

        Filter firstExpectedFilter = new Filter("TempTestBaseInf", "cloudformation", "building");
        Filter secondExpectedFilter = new Filter("SmokeTestNetworkInf", "ansible", "failed");

        assertEquals(firstExpectedFilter, filterList.get(0));
        assertEquals(secondExpectedFilter, filterList.get(1));
    }

    @Test
    public void testMultipleFiltersConvertedWithExtraCommas() {
        String settingInput = ",,TempTestBaseInf:cloudformation:building,,,,SmokeTestNetworkInf:ansible:failed,,,";

        List<Filter> filterList = filterConverter.convertStringToFilterList(settingInput);

        assertNotNull(filterList);
        assertEquals(2, filterList.size());

        Filter firstExpectedFilter = new Filter("TempTestBaseInf", "cloudformation", "building");
        Filter secondExpectedFilter = new Filter("SmokeTestNetworkInf", "ansible", "failed");

        assertEquals(firstExpectedFilter, filterList.get(0));
        assertEquals(secondExpectedFilter, filterList.get(1));
    }

    @Test
    public void testPartiallyDefinedFilter() {
        String settingInput = "TempTestBaseInf";

        List<Filter> filterList = filterConverter.convertStringToFilterList(settingInput);

        assertNotNull(filterList);
        assertEquals(1, filterList.size());

        Filter expectedFilter = new Filter("TempTestBaseInf", null, null);

        assertEquals(expectedFilter, filterList.get(0));
    }

    @Test
    public void testMultiplePartiallyDefinedFilters() {
        String settingInput = "TempTestBaseInf,SmokeTest:cloudformation";

        List<Filter> filterList = filterConverter.convertStringToFilterList(settingInput);

        assertNotNull(filterList);
        assertEquals(2, filterList.size());

        Filter firstExpectedFilter = new Filter("TempTestBaseInf", null, null);
        Filter secondExpectedFilter = new Filter("SmokeTest", "cloudformation", null);

        assertEquals(firstExpectedFilter, filterList.get(0));
        assertEquals(secondExpectedFilter, filterList.get(1));
    }

    @Test
    public void testConvertMultipleFiltersWithWildcards() {
        String settingInput = "*BaseInf*:cloudformation:failed,*NetworkInf*:cloudformation:building";

        List<Filter> filterList = filterConverter.convertStringToFilterList(settingInput);

        assertNotNull(filterList);
        assertEquals(2, filterList.size());

        Filter firstExpectedFilter = new Filter("*BaseInf*", "cloudformation", "failed");
        Filter secondExpectedFilter = new Filter("*NetworkInf*", "cloudformation", "building");

        assertEquals(firstExpectedFilter, filterList.get(0));
        assertEquals(secondExpectedFilter, filterList.get(1));
    }
}