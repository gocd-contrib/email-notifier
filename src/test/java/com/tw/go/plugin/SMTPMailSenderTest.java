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
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SMTPMailSenderTest {
    private GreenMail mailServer;

    @Before
    public void setUp() {
        BasicConfigurator.configure();
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
        assertThat(mailServer.getReceivedMessages()[0].getContent().toString(), is("body\r\n"));
    }

    @After
    public void tearDown() {
        mailServer.stop();
    }
}