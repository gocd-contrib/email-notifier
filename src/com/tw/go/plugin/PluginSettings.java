package com.tw.go.plugin;

import java.util.List;

public class PluginSettings {
    private String smtpHost;
    private int smtpPort;
    private boolean tls;
    private String senderEmailId;
    private String senderPassword;
    private String receiverEmailId;
    private List<Filter> filterList;

    public PluginSettings(String smtpHost, int smtpPort, boolean tls, String senderEmailId, String senderPassword, String receiverEmailId, String filterString) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.tls = tls;
        this.senderEmailId = senderEmailId;
        this.senderPassword = senderPassword;
        this.receiverEmailId = receiverEmailId;
        FilterConverter filterController = new FilterConverter();
        this.filterList = filterController.convertStringToFilterList(filterString);
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public String getSenderEmailId() {
        return senderEmailId;
    }

    public void setSenderEmailId(String senderEmailId) {
        this.senderEmailId = senderEmailId;
    }

    public String getSenderPassword() {
        return senderPassword;
    }

    public void setSenderPassword(String senderPassword) {
        this.senderPassword = senderPassword;
    }

    public String getReceiverEmailId() {
        return receiverEmailId;
    }

    public void setReceiverEmailId(String receiverEmailId) {
        this.receiverEmailId = receiverEmailId;
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginSettings that = (PluginSettings) o;

        if (smtpPort != that.smtpPort) return false;
        if (tls != that.tls) return false;
        if (receiverEmailId != null ? !receiverEmailId.equals(that.receiverEmailId) : that.receiverEmailId != null)
            return false;
        if (senderEmailId != null ? !senderEmailId.equals(that.senderEmailId) : that.senderEmailId != null)
            return false;
        if (senderPassword != null ? !senderPassword.equals(that.senderPassword) : that.senderPassword != null)
            return false;
        if (smtpHost != null ? !smtpHost.equals(that.smtpHost) : that.smtpHost != null)
            return false;
        if(filterList != null ? !filterList.equals(that.filterList) : that.filterList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = smtpHost != null ? smtpHost.hashCode() : 0;
        result = 31 * result + smtpPort;
        result = 31 * result + (tls ? 1 : 0);
        result = 31 * result + (senderEmailId != null ? senderEmailId.hashCode() : 0);
        result = 31 * result + (senderPassword != null ? senderPassword.hashCode() : 0);
        result = 31 * result + (receiverEmailId != null ? receiverEmailId.hashCode() : 0);
        result = 31 * result + (filterList != null ? filterList.hashCode() : 0);
        return result;
    }
}
