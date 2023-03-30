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

import com.thoughtworks.go.plugin.api.logging.Logger;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;

import java.util.Objects;
import java.util.Properties;

import static jakarta.mail.Message.RecipientType.TO;

public class SMTPMailSender {
    private static final Logger LOGGER = Logger.getLoggerFor(EmailNotificationPluginImpl.class);

    private static final String FROM_PROPERTY = "mail.from";
    private static final String TRANSPORT_PROTOCOL_PROPERTY = "mail.transport.protocol";
    private static final String TIMEOUT_PROPERTY = "mail.smtp.timeout";
    private static final String CONNECTION_TIMEOUT_PROPERTY = "mail.smtp.connectiontimeout";
    private static final String STARTTLS_PROPERTY = "mail.smtp.starttls.enable";
    private static final String TLS_CHECK_SERVER_IDENTITY_PROPERTY = "mail.smtp.ssl.checkserveridentity";

    private static final int DEFAULT_MAIL_SENDER_TIMEOUT_IN_MILLIS = 60 * 1000;

    private final SMTPSettings smtpSettings;
    private final SessionFactory sessionFactory;

    public SMTPMailSender(SMTPSettings smtpSettings, SessionFactory sessionFactory) {
        this.smtpSettings = smtpSettings;
        this.sessionFactory = sessionFactory;
    }

    public void send(String subject, String body, String toEmailId) {
        Transport transport = null;
        try {
            Properties properties = mailProperties();
            SessionWrapper sessionWrapper = createSession(properties, smtpSettings.getSmtpUsername(), smtpSettings.getPassword());
            transport = sessionWrapper.getTransport();
            transport.connect(smtpSettings.getHostName(), smtpSettings.getPort(), nullIfEmpty(smtpSettings.getSmtpUsername()), nullIfEmpty(smtpSettings.getPassword()));
            MimeMessage message = sessionWrapper.createMessage(smtpSettings.getFromEmailId(), toEmailId, subject, body);
            transport.sendMessage(message, message.getRecipients(TO));
        } catch (Exception e) {
            LOGGER.error(String.format("Sending failed for email [%s] to [%s]", subject, toEmailId), e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    LOGGER.error("Failed to close transport", e);
                }
            }
        }
    }

    private Properties mailProperties() {
        Properties props = new Properties();
        props.put(FROM_PROPERTY, smtpSettings.getFromEmailId());

        if (!System.getProperties().containsKey(CONNECTION_TIMEOUT_PROPERTY)) {
            props.put(CONNECTION_TIMEOUT_PROPERTY, DEFAULT_MAIL_SENDER_TIMEOUT_IN_MILLIS);
        }

        if (!System.getProperties().containsKey(TIMEOUT_PROPERTY)) {
            props.put(TIMEOUT_PROPERTY, DEFAULT_MAIL_SENDER_TIMEOUT_IN_MILLIS);
        }

        if (System.getProperties().containsKey(STARTTLS_PROPERTY)) {
            props.put(STARTTLS_PROPERTY, "true");
        }

        if (!System.getProperties().containsKey(TLS_CHECK_SERVER_IDENTITY_PROPERTY)) {
            props.put(TLS_CHECK_SERVER_IDENTITY_PROPERTY, "true");
        }

        props.put(TRANSPORT_PROTOCOL_PROPERTY, smtpSettings.isTls() ? "smtps" : "smtp");

        return props;
    }

    private SessionWrapper createSession(Properties properties, String username, String password) {
        if (isEmpty(username) || isEmpty(password)) {
            return sessionFactory.getInstance(properties);
        } else {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtps.auth", "true");
            return sessionFactory.getInstance(properties, new SMTPAuthenticator(username, password));
        }
    }

    private String nullIfEmpty(String str) {
        return isEmpty(str) ? null : str;
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SMTPMailSender that = (SMTPMailSender) o;

        return Objects.equals(smtpSettings, that.smtpSettings);
    }

    @Override
    public int hashCode() {
        return smtpSettings != null ? smtpSettings.hashCode() : 0;
    }
}
