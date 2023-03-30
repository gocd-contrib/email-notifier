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


import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Filter {

    private final String pipelinePattern;
    private final String stagePattern;
    private final BuildState status;

    public Filter(String pipelinePattern, String stagePattern, String status) {
        this.pipelinePattern = pipelinePattern;
        this.stagePattern = stagePattern;
        this.status = BuildState.fromRawString(status);
    }

    public boolean matches(String pipeline, String stage, BuildState status) {
        if (isNotAMatch(pipeline, convertToRegex(this.pipelinePattern))) {
            return false;
        }

        if (isNotAMatch(stage, convertToRegex(this.stagePattern))) {
            return false;
        }

        return this.status == null || this.status.equals(status);
    }

    private boolean isNotAMatch(String value, String pattern) {
        if (isARegex(pattern)) {
            if (!value.toLowerCase().matches(pattern.toLowerCase())) {
                return true;
            }
        } else if (pattern != null && !pattern.equalsIgnoreCase(value)) {
            return true;
        }
        return false;
    }

    private String convertToRegex(String pattern) {
        if (pattern == null || isARegex(pattern)) {
            return pattern;
        }
        return pattern.replaceAll("\\*", ".*");
    }

    private boolean isARegex(String value) {
        try {
            if (value != null) {
                Pattern.compile(value);
                return true;
            } else {
                return false;
            }
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public boolean matches(String pipeline, String stage, String rawStatus) {
        return matches(pipeline, stage, BuildState.fromRawString(rawStatus));
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipelinePattern, stagePattern, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filter filter = (Filter) o;
        return Objects.equals(pipelinePattern, filter.pipelinePattern)
                && Objects.equals(stagePattern, filter.stagePattern)
                && status == filter.status;
    }

    @Override
    public String toString() {
        return "Pipeline: " + pipelinePattern + "\n" +
                "Stage: " + stagePattern + "\n" +
                "Status: " + status + "\n";
    }
}
