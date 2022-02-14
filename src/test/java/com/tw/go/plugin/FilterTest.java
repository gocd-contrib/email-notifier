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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterTest {

    @Test
    public void testSimpleFilterIsMatched() {
        Filter testFilter = new Filter("SmokeTestBaseInf", "cloudformation", "building");

        assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", "building"));

        assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.CANCELLED));
        assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.FAILED));
        assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.FAILING));
        assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.PASSED));
        assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.UNKNOWN));
    }

    @Test
    public void testFilterWithNullBuildStateMatchesAllStates() {
        Filter testFilter = new Filter("SmokeTestBaseInf", "cloudformation", null);

        for(BuildState currentState : BuildState.values()) {
            assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", currentState));
            assertTrue(testFilter.matches("SmokeTestBaseInf", "ClOuDfOrMaTiOn", currentState));
        }
    }

    @Test
    public void testFilterWithNullStageNameAndBuildStateMatchesAllStates() {
        Filter testFilter = new Filter("SmokeTestBaseInf", null, null);

        for(BuildState currentState : BuildState.values()) {
            assertTrue(testFilter.matches("SmokeTestBaseInf", null, currentState));
        }
    }

    @Test
    public void testFilterWithNullStageNameAndBuildStateMatchesAllStageNames() {
        Filter testFilter = new Filter("SmokeTestBaseInf", null, null);

        assertTrue(testFilter.matches("SmokeTestBaseInf", null, BuildState.BUILDING));
        assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("SmokeTestBaseInf", "CLOUDforMATION", BuildState.BUILDING));
        assertTrue(testFilter.matches("SmokeTestBaseInf", "somethingrandom", BuildState.BUILDING));
    }

    @Test
    public void testFilterWithWilcardPipelineNameMatchesAllPipelines() {
        Filter testFilter = new Filter("*BaseInf*", "cloudformation", "building");

        assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("TempTestBaseInf", "cloudformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("ProductionBaseInf", "cloudformation", BuildState.BUILDING));
    }

    @Test
    public void testFilterWithRegexPipelineNameMatchesPipelines() {
        Filter testFilter = new Filter(".*BaseInf", "cloudformation", "building");

        assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("TempTestBaseInf", "cloudformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("ProductionBaseInf", "cloudformation", BuildState.BUILDING));
    }

    @Test
    public void testFilterWithRegexStageNameMatchesStages() {
        Filter testFilter = new Filter("SmokeTestBaseInf", ".*formation", "building");

        assertTrue(testFilter.matches("SmokeTestBaseInf", "bananaformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("SmokeTestBaseInf", "pickleformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
    }

    @Test
    public void testFilterWithRegexStageAndPipelineNameMatchesStages() {
        Filter testFilter = new Filter(".*BaseInf", ".*formation", "building");

        assertTrue(testFilter.matches("SmokeTestBaseInf", "bananaformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("TempTestBaseInf", "pickleformation", BuildState.BUILDING));
        assertTrue(testFilter.matches("ProductionBaseInf", "cloudformation", BuildState.BUILDING));
    }
}