package com.tw.go.plugin;


public enum BuildState {

    BUILDING,
    FAILING,
    PASSED,
    FAILED,
    CANCELLED,
    UNKNOWN;

    public static BuildState fromRawString(String rawString) {
        if(rawString == null) {
            return null;
        }
        switch (rawString.toLowerCase()) {
            case "building":
                return BUILDING;
            case "failing":
                return FAILING;
            case "passed":
                return PASSED;
            case "failed":
                return FAILED;
            case "cancelled":
                return CANCELLED;
            default:
                return UNKNOWN;
        }
    }
}
