package com.tw.go.plugin;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

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
        boolean pipelineIsRegex;
        boolean stageIsRegex;

        //Check if pipeline string is a regex
        try {
            if(this.pipeline != null) {
                Pattern.compile(this.pipeline);
                pipelineIsRegex = true;
            } else {
                pipelineIsRegex = false;
            }
        } catch (PatternSyntaxException e) {
            pipelineIsRegex = false;
        }

        //Check if stage string is a regex
        try {
            if(this.stage != null) {
                Pattern.compile(this.stage);
                stageIsRegex = true;
            } else {
                stageIsRegex = false;
            }
        } catch (PatternSyntaxException e) {
            stageIsRegex = false;
        }

        if(pipelineIsRegex) {
            Pattern pat = Pattern.compile(this.pipeline,Pattern.CASE_INSENSITIVE);
            Matcher m = pat.matcher(pipeline);

            if(!m.find()) {
                return false;
            }
        } else if(this.pipeline.startsWith("*") && this.pipeline.endsWith("*")) {
            if(pipeline == null || !pipeline.contains(this.pipeline.replaceAll("\\*", ""))) {
                return false;
            }
        } else if(this.pipeline != null && !this.pipeline.equalsIgnoreCase(pipeline)) {
            return false;
        }

        if(stageIsRegex) {
            Pattern pat = Pattern.compile(this.stage,Pattern.CASE_INSENSITIVE);
            Matcher m = pat.matcher(stage);

            if(!m.find()) {
                return false;
            }
        } else if(this.stage != null && !this.stage.equalsIgnoreCase(stage)) {
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
