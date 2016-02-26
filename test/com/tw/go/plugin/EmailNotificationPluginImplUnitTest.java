package com.tw.go.plugin;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.tw.go.plugin.util.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Session.class)
public class EmailNotificationPluginImplUnitTest {

    @Mock
    private GoApplicationAccessor goApplicationAccessor;

    @Mock
    private Session mockSession;

    @Mock
    private Transport mockTransport;

    @Mock
    private Properties mockProperties;

    private Map<String, Object> settingsResponseMap;

    private Map<String, Object> stateChangeResponseMap;


    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockStatic(Session.class);

        when(mockSession.getTransport()).thenReturn(mockTransport);
        when(mockSession.getTransport(any(Address.class))).thenReturn(mockTransport);
        when(mockSession.getTransport(any(Provider.class))).thenReturn(mockTransport);

        when(mockSession.getProperties()).thenReturn(mockProperties);

        when(Session.getInstance(any(Properties.class))).thenReturn(mockSession);
        when(Session.getInstance(any(Properties.class), any(Authenticator.class))).thenReturn(mockSession);


        emailNotificationPlugin = new EmailNotificationPluginImpl();
        emailNotificationPlugin.initializeGoApplicationAccessor(goApplicationAccessor);
    }

    @Before
    public void setupDefaultSettingResponse() {
        settingsResponseMap = new HashMap<>();

        settingsResponseMap.put("smtp_host", "test-smtp-host");
        settingsResponseMap.put("smtp_port", "25");
        settingsResponseMap.put("is_tls", "0");
        settingsResponseMap.put("sender_email_id", "test-smtp-sender");
        settingsResponseMap.put("sender_password", "test-smtp-password");
        settingsResponseMap.put("receiver_email_id", "test-smtp-receiver");
    }

    @Before
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

    private EmailNotificationPluginImpl emailNotificationPlugin;

    @Test
    public void testStageNotificationRequestsSettings() throws Exception {
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

        GoPluginApiRequest requestFromServer = testStageChangeRequestFromServer();

        emailNotificationPlugin.handle(requestFromServer);

        verify(mockTransport).sendMessage(any(Message.class), eq( new Address[] { new InternetAddress("test-email@test.co.uk") } ));
        verify(mockTransport, times(1)).connect(eq("test-smtp-host"), eq("test-smtp-sender"), eq("test-smtp-password"));
        verify(mockTransport, times(1)).close();
        verifyNoMoreInteractions(mockTransport);
    }

    @Test
    public void testMultipleEmailAddressSendsEmail() throws Exception {
        settingsResponseMap.put("receiver_email_id", "test-email@test.co.uk, test-email-2@test.co.uk");

        GoApiResponse settingsResponse = testSettingsResponse();

        when(goApplicationAccessor.submit(any(GoApiRequest.class))).thenReturn(settingsResponse);

        GoPluginApiRequest requestFromServer = testStageChangeRequestFromServer();

        emailNotificationPlugin.handle(requestFromServer);

        verify(mockTransport).sendMessage(any(Message.class), eq( new Address[] { new InternetAddress("test-email@test.co.uk") } ));
        verify(mockTransport).sendMessage(any(Message.class), eq( new Address[] { new InternetAddress("test-email-2@test.co.uk") } ));
        verify(mockTransport, times(2)).connect(eq("test-smtp-host"), eq("test-smtp-sender"), eq("test-smtp-password"));
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

        final Map<String, Object> requestMap = new HashMap<String, Object>();
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