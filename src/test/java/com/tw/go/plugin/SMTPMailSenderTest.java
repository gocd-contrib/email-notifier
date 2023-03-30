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

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.mail.MessagingException;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SMTPMailSenderTest {
    private GreenMail mailServer;

    @BeforeEach
    public void setUp() {
        mailServer = new GreenMail(ServerSetupTest.SMTP);
        mailServer.start();
    }

    @Test
    public void shouldSendEmail() throws MessagingException, IOException {
        String userName = "user1";
        String emailId = "user1@domain.com";
        String password = "password1";

        mailServer.setUser(emailId, userName, password);
        SMTPSettings settings = new SMTPSettings("127.0.0.1", ServerSetupTest.SMTP.getPort(), false, emailId, userName, password);
        new SMTPMailSender(settings, new SessionFactory()).send("subject", "body", emailId);

        assertThat(mailServer.getReceivedMessages().length, is(1));
        assertThat(mailServer.getReceivedMessages()[0].getFrom()[0].toString(), is(emailId));
        assertThat(mailServer.getReceivedMessages()[0].getSubject(), is("subject"));
        assertThat(mailServer.getReceivedMessages()[0].getContent().toString(), is("body"));
    }

    @AfterEach
    public void tearDown() {
        mailServer.stop();
    }
}