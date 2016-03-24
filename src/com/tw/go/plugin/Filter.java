package com.tw.go.plugin;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Filter {

    private String pipeline;
    private String stage;
    private BuildState status;

    public Filter(String pipeline, String stage, String status) {
        this.pipeline = pipeline;
        this.stage = stage;
        this.status = BuildState.fromRawString(status);
    }

    public boolean matches(String pipeline, String stage, BuildState status) {
        if(this.pipeline != null) {

            if(this.pipeline.startsWith("*") && this.pipeline.endsWith("*")) {
                if(pipeline == null || !pipeline.contains(this.pipeline.replaceAll("\\*", ""))) {
                    return false;
                }
            } else if(!this.pipeline.equalsIgnoreCase(pipeline)) {
                return false;
            }
        }

        if(this.stage != null && !this.stage.equalsIgnoreCase(stage)) {
            return false;
        }

        if(this.status != null && !this.status.equals(status)){
            return false;
        }

        return true;
    }

    public boolean matches(String pipeline, String stage, String rawStatus) {
        return matches(pipeline, stage, BuildState.fromRawString(rawStatus));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(pipeline)
                .append(stage)
                .append(status)
                .hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Filter)) {
            return false;
        }
        Filter otherFilter = (Filter) other;

        return new EqualsBuilder()
                .append(this.pipeline, otherFilter.pipeline)
                .append(this.stage, otherFilter.stage)
                .append(this.status, otherFilter.status)
                .build();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Pipeline: "+pipeline+"\n");
        stringBuilder.append("Stage: "+stage+"\n");
        stringBuilder.append("Status: "+status+"\n");

        return stringBuilder.toString();
    }
}
