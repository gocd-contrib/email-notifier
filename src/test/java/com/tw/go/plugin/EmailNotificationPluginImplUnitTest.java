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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.tw.go.plugin.util.JSONUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class EmailNotificationPluginImplUnitTest {

    @Mock
    private GoApplicationAccessor goApplicationAccessor;

    @Mock
    private SessionWrapper mockSession;

    @Mock
    private Transport mockTransport;

    @Mock
    private SessionFactory mockSessionFactory;

    private Map<String, Object> settingsResponseMap;

    private Map<String, Object> stateChangeResponseMap;
    private EmailNotificationPluginImpl emailNotificationPlugin;


    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mockSession.getTransport()).thenReturn(mockTransport);

        when(mockSessionFactory.getInstance(any(Properties.class))).thenReturn(mockSession);
        when(mockSessionFactory.getInstance(any(Properties.class), any(Authenticator.class))).thenReturn(mockSession);

        emailNotificationPlugin = new EmailNotificationPluginImpl();
        emailNotificationPlugin.initializeGoApplicationAccessor(goApplicationAccessor);
        emailNotificationPlugin.setSessionFactory(mockSessionFactory);
    }

    @BeforeEach
    public void setupDefaultSettingResponse() {
        settingsResponseMap = new HashMap<>();

        settingsResponseMap.put("smtp_host", "test-smtp-host");
        settingsResponseMap.put("smtp_port", "25");
        settingsResponseMap.put("is_tls", "0");
        settingsResponseMap.put("sender_email_id", "test-smtp-sender");
        settingsResponseMap.put("sender_password", "test-smtp-password");
        settingsResponseMap.put("smtp_username", "test-smtp-username");
        settingsResponseMap.put("receiver_email_id", "test-smtp-receiver");
    }

    @BeforeEach
    public void setupDefaultStateChangeResponseMap() {
        Map<String, Object> stageResponseMap = new HashMap<>();

        stageResponseMap.put("name", "test-stage-name");
        stageResponseMap.put("counter", "test-counter");
        stageResponseMap.put("state", "test-state");
        stageResponseMap.put("result", "test-result");
        stageResponseMap.put("last-transition-time", "test-last-transition-time");
        stageResponseMap.put("create-time", "test-last-transition-time");


        Map<String, Object> pipelineMap = new HashMap<>();

        pipelineMap.put("stage", stageResponseMap);
        pipelineMap.put("name", "test-pipeline-name");
        pipelineMap.put("counter", "test-pipeline-counter");

        stateChangeResponseMap = new HashMap<>();

        stateChangeResponseMap.put("pipeline", pipelineMap);
    }


    @Test
    public void testStageNotificationRequestsSettings() {
        GoApiResponse settingsResponse = testSettingsResponse();

        when(goApplicationAccessor.submit(eq(testSettingsRequest()))).thenReturn(settingsResponse);

        GoPluginApiRequest requestFromServer = testStageChangeRequestFromServer();

        emailNotificationPlugin.handle(requestFromServer);

        final ArgumentCaptor<GoApiRequest> settingsRequestCaptor = ArgumentCaptor.forClass(GoApiRequest.class);

        verify(goApplicationAccessor).submit(settingsRequestCaptor.capture());

        final GoApiRequest actualSettingsRequest = settingsRequestCaptor.getValue();

        assertEquals(testSettingsRequest().api(), actualSettingsRequest.api());
        assertEquals(testSettingsRequest().apiVersion(), actualSettingsRequest.apiVersion());

        GoPluginIdentifier actualGoPluginIdentifier = actualSettingsRequest.pluginIdentifier();

        assertNotNull(actualGoPluginIdentifier);

        assertEquals(testSettingsRequest().pluginIdentifier().getExtension(), actualGoPluginIdentifier.getExtension());
        assertEquals(testSettingsRequest().pluginIdentifier().getSupportedExtensionVersions(), actualGoPluginIdentifier.getSupportedExtensionVersions());
        assertEquals(testSettingsRequest().requestBody(), actualSettingsRequest.requestBody());
        assertEquals(testSettingsRequest().requestHeaders(), actualSettingsRequest.requestHeaders());
        assertEquals(testSettingsRequest().requestParameters(), actualSettingsRequest.requestParameters());
    }

    @Test
    public void testASingleEmailAddressSendsEmail() throws Exception {
        settingsResponseMap.put("receiver_email_id", "test-email@test.co.uk");

        GoApiResponse settingsResponse = testSettingsResponse();

        when(goApplicationAccessor.submit(any(GoApiRequest.class))).thenReturn(settingsResponse);
        doCallRealMethod().when(mockSession).createMessage(anyString(), anyString(), anyString(), anyString());

        GoPluginApiRequest requestFromServer = testStageChangeRequestFromServer();

        emailNotificationPlugin.handle(requestFromServer);

        verify(mockTransport).sendMessage(any(Message.class), eq(new Address[]{new InternetAddress("test-email@test.co.uk")}));
        verify(mockTransport, times(1)).connect(eq("test-smtp-host"), eq(25), eq("test-smtp-username"), eq("test-smtp-password"));
        verify(mockTransport, times(1)).close();
        verifyNoMoreInteractions(mockTransport);
    }

    @Test
    public void testMultipleEmailAddressSendsEmail() throws Exception {
        settingsResponseMap.put("receiver_email_id", "test-email@test.co.uk, test-email-2@test.co.uk");

        GoApiResponse settingsResponse = testSettingsResponse();

        when(goApplicationAccessor.submit(any(GoApiRequest.class))).thenReturn(settingsResponse);
        doCallRealMethod().when(mockSession).createMessage(anyString(), anyString(), anyString(), anyString());

        GoPluginApiRequest requestFromServer = testStageChangeRequestFromServer();

        emailNotificationPlugin.handle(requestFromServer);

        verify(mockTransport).sendMessage(any(Message.class), eq(new Address[]{new InternetAddress("test-email@test.co.uk")}));
        verify(mockTransport).sendMessage(any(Message.class), eq(new Address[]{new InternetAddress("test-email-2@test.co.uk")}));
        verify(mockTransport, times(2)).connect(eq("test-smtp-host"), eq(25), eq("test-smtp-username"), eq("test-smtp-password"));
        verify(mockTransport, times(2)).close();
        verifyNoMoreInteractions(mockTransport);
    }


    private GoPluginApiRequest testStageChangeRequestFromServer() {
        GoPluginApiRequest requestFromGoServer = mock(GoPluginApiRequest.class);

        when(requestFromGoServer.requestName()).thenReturn("stage-status");

        when(requestFromGoServer.requestBody()).thenReturn(JSONUtils.toJSON(stateChangeResponseMap));

        return requestFromGoServer;
    }

    private static GoApiRequest testSettingsRequest() {

        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("plugin-id", "email.notifier");

        final String responseBody = JSONUtils.toJSON(requestMap);

        return new GoApiRequest() {
            @Override
            public String api() {
                return "go.processor.plugin-settings.get";
            }

            @Override
            public String apiVersion() {
                return "1.0";
            }

            @Override
            public GoPluginIdentifier pluginIdentifier() {
                return new GoPluginIdentifier("notification", Collections.singletonList("1.0"));
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

    private GoApiResponse testSettingsResponse() {
        GoApiResponse settingsResponse = mock(GoApiResponse.class);

        when(settingsResponse.responseBody()).thenReturn(JSONUtils.toJSON(settingsResponseMap));

        return settingsResponse;
    }


}