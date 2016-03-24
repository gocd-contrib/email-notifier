package com.tw.go.plugin;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FilterConverterTest {

    private FilterConverter filterConverter;

    @Before
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