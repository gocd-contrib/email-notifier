/*
 * Copyright 2018 ThoughtWorks, Inc.
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

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

import static javax.mail.Message.RecipientType.TO;

public class SessionWrapper {
    private Session instance;

    public SessionWrapper(Session instance) {
        this.instance = instance;
    }

    public Transport getTransport() throws NoSuchProviderException {
        return instance.getTransport();
    }

    public MimeMessage createMessage(String fromEmailId, String toEmailId, String subject, String body) throws MessagingException {
        MimeMessage message = new MimeMessage(instance);
        message.setFrom(new InternetAddress(fromEmailId));
        message.setRecipients(TO, toEmailId);
        message.setSubject(subject);
        message.setContent(message, "text/plain");
        message.setSentDate(new Date());
        message.setText(body);
        message.setSender(new InternetAddress(fromEmailId));
        message.setReplyTo(new InternetAddress[]{new InternetAddress(fromEmailId)});
        return message;
    }
}
