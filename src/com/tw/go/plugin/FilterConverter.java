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

import java.util.ArrayList;
import java.util.List;

public class FilterConverter {

    private static final String FILTER_SEPARATOR = ",";
    private static final String FILTER_FIELD_SEPARATOR = ":";

    public List<Filter> convertStringToFilterList(String filterConfigString) {
        ArrayList<Filter> filterList = new ArrayList<>();

        if(filterConfigString == null || "".equals(filterConfigString)) {
            return filterList;
        }

        String[] filterConfigList = convertToList(filterConfigString);

        for(String filterConfig : filterConfigList) {
            Filter converted = convertToFilter(filterConfig);
            if(converted != null) {
                filterList.add(converted);
            }
        }

        return filterList;
    }


    /**
     * Assumes a list of values separated by FILTER_SEPARATOR
     *
     * Each value will be treated as an individual filter
     *
     * E.g.
     *
     * "TempTest:cloudformation:building,SmokeTest:cloudformation:failed"
     * Would become
     * [
     * "TempTest:cloudformation:building",
     * "SmokeTest:cloudformation:failed"
     * ]
     */
    private String[] convertToList(String configList) {
        if(configList == null || "".equals(configList)) {
            return new String[0];
        }

        String[] filterConfigList = new String[] { configList };

        if(configList.contains(FILTER_SEPARATOR)) {
            filterConfigList = configList.split(FILTER_SEPARATOR);
        }

        return filterConfigList;
    }

    /**
     * Assumes each filter has at most 3 fields (pipeline/stage/status) split by FILTER_FIELD_SEPARATOR
     *
     * E.g.
     *
     * TempTestDeploy:cloudformation:building
     *
     * becomes a filter object with the 3 values set appropriately
     */
    private Filter convertToFilter(String filterConfig) {
        if(filterConfig == null || "".equals(filterConfig)) {
            return null;
        }
        String pipeline = filterConfig;
        String stage = null;
        String status = null;

        if(filterConfig.contains(FILTER_FIELD_SEPARATOR)) {
            String[] filterConfigParts = filterConfig.split(FILTER_FIELD_SEPARATOR);

            pipeline = filterConfigParts[0];

            if(filterConfigParts.length > 1) {
                stage = filterConfigParts[1];
            }

            if(filterConfigParts.length > 2) {
                status = filterConfigParts[2];
            }

        }

        return new Filter(pipeline, stage, status);
    }

}
