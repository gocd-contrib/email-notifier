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

public class SMTPSettings {
    private String hostName;
    private int port;
    private boolean tls;
    private String fromEmailId;
    private String password;
    private String smtpUsername;

    public SMTPSettings(String hostName, int port, boolean tls, String fromEmailId, String smtpUsername, String password) {
        this.hostName = hostName;
        this.port = port;
        this.tls = tls;
        this.password = password;
        this.smtpUsername = smtpUsername;
        this.fromEmailId = fromEmailId;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public boolean isTls() {
        return tls;
    }

    public String getFromEmailId() {
        return fromEmailId;
    }

    public String getPassword() {
        return password;
    }

    public String getSmtpUsername() { return smtpUsername; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SMTPSettings that = (SMTPSettings) o;

        if (port != that.port) return false;
        if (tls != that.tls) return false;
        if (fromEmailId != null ? !fromEmailId.equals(that.fromEmailId) : that.fromEmailId != null) return false;
        if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (smtpUsername != null ? !smtpUsername.equals(that.smtpUsername) : that.smtpUsername != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hostName != null ? hostName.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (tls ? 1 : 0);
        result = 31 * result + (fromEmailId != null ? fromEmailId.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (smtpUsername != null ? smtpUsername.hashCode() : 0);
        return result;
    }
}
