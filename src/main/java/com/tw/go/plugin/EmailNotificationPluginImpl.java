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

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.plugin.util.FieldValidator;
import com.tw.go.plugin.util.JSONUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;

@Extension
public class EmailNotificationPluginImpl implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(EmailNotificationPluginImpl.class);

    public static final String PLUGIN_ID = "email.notifier";
    public static final String EXTENSION_NAME = "notification";
    private static final List<String> goSupportedVersions = asList("1.0");

    public static final String PLUGIN_SETTINGS_SMTP_HOST = "smtp_host";
    public static final String PLUGIN_SETTINGS_SMTP_PORT = "smtp_port";
    public static final String PLUGIN_SETTINGS_IS_TLS = "is_tls";
    public static final String PLUGIN_SETTINGS_SENDER_EMAIL_ID = "sender_email_id";
    public static final String PLUGIN_SETTINGS_SMTP_USERNAME = "smtp_username";
    public static final String PLUGIN_SETTINGS_SENDER_PASSWORD = "sender_password";
    public static final String PLUGIN_SETTINGS_RECEIVER_EMAIL_ID = "receiver_email_id";
    public static final String PLUGIN_SETTINGS_FILTER = "pipeline_stage_filter";

    public static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String PLUGIN_SETTINGS_GET_VIEW = "go.plugin-settings.get-view";
    public static final String PLUGIN_SETTINGS_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
    public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    public static final String REQUEST_STAGE_STATUS = "stage-status";

    public static final String GET_PLUGIN_SETTINGS = "go.processor.plugin-settings.get";

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int NOT_FOUND_RESPONSE_CODE = 404;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    private GoApplicationAccessor goApplicationAccessor;
    private SessionFactory sessionFactory;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
        this.sessionFactory = new SessionFactory();
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        String requestName = goPluginApiRequest.requestName();
        if (requestName.equals(PLUGIN_SETTINGS_GET_CONFIGURATION)) {
            return handleGetPluginSettingsConfiguration();
        } else if (requestName.equals(PLUGIN_SETTINGS_GET_VIEW)) {
            try {
                return handleGetPluginSettingsView();
            } catch (IOException e) {
                return renderJSON(500, String.format("Failed to find template: %s", e.getMessage()));
            }
        } else if (requestName.equals(PLUGIN_SETTINGS_VALIDATE_CONFIGURATION)) {
            return handleValidatePluginSettingsConfiguration(goPluginApiRequest);
        } else if (requestName.equals(REQUEST_NOTIFICATIONS_INTERESTED_IN)) {
            return handleNotificationsInterestedIn();
        } else if (requestName.equals(REQUEST_STAGE_STATUS)) {
            return handleStageNotification(goPluginApiRequest);
        }
        return renderJSON(NOT_FOUND_RESPONSE_CODE, null);
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return getGoPluginIdentifier();
    }

    private GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", Arrays.asList(REQUEST_STAGE_STATUS));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> dataMap = (Map<String, Object>) JSONUtils.fromJSON(goPluginApiRequest.requestBody());

        int responseCode = SUCCESS_RESPONSE_CODE;
        Map<String, Object> response = new HashMap<>();
        List<String> messages = new ArrayList<>();
        try {
            Map<String, Object> pipelineMap = (Map<String, Object>) dataMap.get("pipeline");
            Map<String, Object> stageMap = (Map<String, Object>) pipelineMap.get("stage");


            String pipelineName = (String) pipelineMap.get("name");
            String stageName = (String) stageMap.get("name");
            String stageState = (String) stageMap.get("state");

            String subject = String.format("%s/%s is/has %s", pipelineName, stageName, stageState);
            String body = String.format("State: %s\nResult: %s\nCreate Time: %s\nLast Transition Time: %s", stageState, stageMap.get("result"), stageMap.get("create-time"), stageMap.get("last-transition-time"));

            PluginSettings pluginSettings = getPluginSettings();


            boolean matchesFilter = false;

            List<Filter> filterList = pluginSettings.getFilterList();

            if(filterList.isEmpty()) {
                matchesFilter = true;
            } else {
                for(Filter filter : filterList) {
                    if(filter.matches(pipelineName, stageName, stageState)) {
                        matchesFilter = true;
                    }
                }
            }

            if(matchesFilter) {
                LOGGER.info("Sending Email for " + subject);

                String receiverEmailIdString = pluginSettings.getReceiverEmailId();

                String[] receiverEmailIds = new String[]{receiverEmailIdString};

                if (receiverEmailIdString.contains(",")) {
                    receiverEmailIds = receiverEmailIdString.split(",");
                }

                for (String receiverEmailId : receiverEmailIds) {
                    SMTPSettings settings = new SMTPSettings(pluginSettings.getSmtpHost(), pluginSettings.getSmtpPort(), pluginSettings.isTls(), pluginSettings.getSenderEmailId(), pluginSettings.getSmtpUsername(), pluginSettings.getSenderPassword());
                    new SMTPMailSender(settings, sessionFactory).send(subject, body, receiverEmailId);
                }

                LOGGER.info("Successfully delivered an email.");
            } else {
                LOGGER.info("Skipped email as no filter matched this pipeline/stage/state");
            }

            response.put("status", "success");
        } catch (Exception e) {
            LOGGER.warn("Error occurred while trying to deliver an email.", e);

            responseCode = INTERNAL_ERROR_RESPONSE_CODE;
            response.put("status", "failure");
            if (!isEmpty(e.getMessage())) {
                messages.add(e.getMessage());
            }
        }

        if (!messages.isEmpty()) {
            response.put("messages", messages);
        }
        return renderJSON(responseCode, response);
    }

    private GoPluginApiResponse handleGetPluginSettingsConfiguration() {
        Map<String, Object> response = new HashMap<>();
        response.put(PLUGIN_SETTINGS_SMTP_HOST, createField("SMTP Host", null, true, false, "0"));
        response.put(PLUGIN_SETTINGS_SMTP_PORT, createField("SMTP Port", null, true, false, "1"));
        response.put(PLUGIN_SETTINGS_IS_TLS, createField("TLS", null, true, false, "2"));
        response.put(PLUGIN_SETTINGS_SENDER_EMAIL_ID, createField("Sender Email ID", null, true, false, "3"));
        response.put(PLUGIN_SETTINGS_SMTP_USERNAME, createField("SMTP Username", null, false, false, "4"));
        response.put(PLUGIN_SETTINGS_SENDER_PASSWORD, createField("Sender Password", null, false, true, "5"));
        response.put(PLUGIN_SETTINGS_RECEIVER_EMAIL_ID, createField("Receiver Email-id", null, true, false, "6"));
        response.put(PLUGIN_SETTINGS_FILTER, createField("Pipeline/Stage/Status filter", null, false, false, "7"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private Map<String, Object> createField(String displayName, String defaultValue, boolean isRequired, boolean isSecure, String displayOrder) {
        Map<String, Object> fieldProperties = new HashMap<>();
        fieldProperties.put("display-name", displayName);
        fieldProperties.put("default-value", defaultValue);
        fieldProperties.put("required", isRequired);
        fieldProperties.put("secure", isSecure);
        fieldProperties.put("display-order", displayOrder);
        return fieldProperties;
    }

    private GoPluginApiResponse handleGetPluginSettingsView() throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("template", IOUtils.toString(getClass().getResourceAsStream("/plugin-settings.template.html"), "UTF-8"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleValidatePluginSettingsConfiguration(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) JSONUtils.fromJSON(goPluginApiRequest.requestBody());
        final Map<String, String> configuration = keyValuePairs(responseMap, "plugin-settings");
        List<Map<String, Object>> response = new ArrayList<>();

        validate(response, new FieldValidator() {
            @Override
            public void validate(Map<String, Object> fieldValidation) {
                validateRequiredField(configuration, fieldValidation, PLUGIN_SETTINGS_SMTP_HOST, "SMTP Host");
            }
        });

        validate(response, new FieldValidator() {
            @Override
            public void validate(Map<String, Object> fieldValidation) {
                validateRequiredField(configuration, fieldValidation, PLUGIN_SETTINGS_SMTP_PORT, "SMTP Port");
            }
        });

        validate(response, new FieldValidator() {
            @Override
            public void validate(Map<String, Object> fieldValidation) {
                validateRequiredField(configuration, fieldValidation, PLUGIN_SETTINGS_IS_TLS, "TLS");
            }
        });

        validate(response, new FieldValidator() {
            @Override
            public void validate(Map<String, Object> fieldValidation) {
                validateRequiredField(configuration, fieldValidation, PLUGIN_SETTINGS_SENDER_EMAIL_ID, "Sender Email ID");
            }
        });

        validate(response, new FieldValidator() {
            @Override
            public void validate(Map<String, Object> fieldValidation) {
                validateRequiredField(configuration, fieldValidation, PLUGIN_SETTINGS_RECEIVER_EMAIL_ID, "Receiver Email-id");
            }
        });

        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private void validate(List<Map<String, Object>> response, FieldValidator fieldValidator) {
        Map<String, Object> fieldValidation = new HashMap<>();
        fieldValidator.validate(fieldValidation);
        if (!fieldValidation.isEmpty()) {
            response.add(fieldValidation);
        }
    }

    private void validateRequiredField(Map<String, String> configuration, Map<String, Object> fieldMap, String key, String name) {
        if (configuration.get(key) == null || configuration.get(key).isEmpty()) {
            fieldMap.put("key", key);
            fieldMap.put("message", String.format("'%s' is a required field", name));
        }
    }

    public PluginSettings getPluginSettings() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("plugin-id", PLUGIN_ID);
        GoApiResponse response = goApplicationAccessor.submit(createGoApiRequest(GET_PLUGIN_SETTINGS, JSONUtils.toJSON(requestMap)));
        if (response.responseBody() == null || response.responseBody().trim().isEmpty()) {
            throw new RuntimeException("plugin is not configured. please provide plugin settings.");
        }
        Map<String, String> responseBodyMap = (Map<String, String>) JSONUtils.fromJSON(response.responseBody());
        return new PluginSettings(responseBodyMap.get(PLUGIN_SETTINGS_SMTP_HOST), Integer.parseInt(responseBodyMap.get(PLUGIN_SETTINGS_SMTP_PORT)),
                Boolean.parseBoolean(responseBodyMap.get(PLUGIN_SETTINGS_IS_TLS)), responseBodyMap.get(PLUGIN_SETTINGS_SENDER_EMAIL_ID),
                responseBodyMap.get(PLUGIN_SETTINGS_SMTP_USERNAME),
                responseBodyMap.get(PLUGIN_SETTINGS_SENDER_PASSWORD), responseBodyMap.get(PLUGIN_SETTINGS_RECEIVER_EMAIL_ID),
                responseBodyMap.get(PLUGIN_SETTINGS_FILTER));
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private Map<String, String> keyValuePairs(Map<String, Object> map, String mainKey) {
        Map<String, String> keyValuePairs = new HashMap<>();
        Map<String, Object> fieldsMap = (Map<String, Object>) map.get(mainKey);
        for (String field : fieldsMap.keySet()) {
            Map<String, Object> fieldProperties = (Map<String, Object>) fieldsMap.get(field);
            String value = (String) fieldProperties.get("value");
            keyValuePairs.put(field, value);
        }
        return keyValuePairs;
    }

    private GoPluginIdentifier getGoPluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
    }

    private GoApiRequest createGoApiRequest(final String api, final String responseBody) {
        return new GoApiRequest() {
            @Override
            public String api() {
                return api;
            }

            @Override
            public String apiVersion() {
                return "1.0";
            }

            @Override
            public GoPluginIdentifier pluginIdentifier() {
                return getGoPluginIdentifier();
            }

            @Override
            public Map<String, String> requestParameters() {
                return null;
            }

            @Override
            public Map<String, String> requestHeaders() {
                return null;
            }

            @Override
            public String requestBody() {
                return responseBody;
            }
        };
    }

    private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().create().toJson(response);
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }

    void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
