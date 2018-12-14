package com.tw.go.plugin;


import org.junit.Assert;
import org.junit.Test;

public class FilterTest {

    @Test
    public void testSimpleFilterIsMatched() {
        Filter testFilter = new Filter("SmokeTestBaseInf", "cloudformation", "building");

        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", "building"));

        Assert.assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.CANCELLED));
        Assert.assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.FAILED));
        Assert.assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.FAILING));
        Assert.assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.PASSED));
        Assert.assertFalse(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.UNKNOWN));
    }

    @Test
    public void testFilterWithNullBuildStateMatchesAllStates() {
        Filter testFilter = new Filter("SmokeTestBaseInf", "cloudformation", null);

        for(BuildState currentState : BuildState.values()) {
            Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", currentState));
            Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "ClOuDfOrMaTiOn", currentState));
        }
    }

    @Test
    public void testFilterWithNullStageNameAndBuildStateMatchesAllStates() {
        Filter testFilter = new Filter("SmokeTestBaseInf", null, null);

        for(BuildState currentState : BuildState.values()) {
            Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", null, currentState));
        }
    }

    @Test
    public void testFilterWithNullStageNameAndBuildStateMatchesAllStageNames() {
        Filter testFilter = new Filter("SmokeTestBaseInf", null, null);

        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", null, BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "CLOUDforMATION", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "somethingrandom", BuildState.BUILDING));
    }

    @Test
    public void testFilterWithWilcardPipelineNameMatchesAllPipelines() {
        Filter testFilter = new Filter("*BaseInf*", "cloudformation", "building");

        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("TempTestBaseInf", "cloudformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("ProductionBaseInf", "cloudformation", BuildState.BUILDING));
    }

    @Test
    public void testFilterWithRegexPipelineNameMatchesPipelines() {
        Filter testFilter = new Filter(".*BaseInf", "cloudformation", "building");

        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("TempTestBaseInf", "cloudformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("ProductionBaseInf", "cloudformation", BuildState.BUILDING));
    }

    @Test
    public void testFilterWithRegexStageNameMatchesStages() {
        Filter testFilter = new Filter("SmokeTestBaseInf", ".*formation", "building");

        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "bananaformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "pickleformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "cloudformation", BuildState.BUILDING));
    }

    @Test
    public void testFilterWithRegexStageAndPipelineNameMatchesStages() {
        Filter testFilter = new Filter(".*BaseInf", ".*formation", "building");

        Assert.assertTrue(testFilter.matches("SmokeTestBaseInf", "bananaformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("TempTestBaseInf", "pickleformation", BuildState.BUILDING));
        Assert.assertTrue(testFilter.matches("ProductionBaseInf", "cloudformation", BuildState.BUILDING));
    }
}