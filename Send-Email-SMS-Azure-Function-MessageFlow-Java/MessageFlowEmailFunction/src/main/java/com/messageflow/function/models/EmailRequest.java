package com.messageflow.function.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class EmailRequest {
    private String subject;

    @SerializedName("smtpAccount")
    private String smtpAccount;

    private EmailRecipient from;
    private List<EmailRecipient> to;
    private EmailContent content;
    private List<String> tags;
    private List<EmailRecipient> cc;
    private List<EmailRecipient> bcc;

    @SerializedName("replyTo")
    private EmailRecipient replyTo;

    private Map<String, String> headers;

    @SerializedName("globalVars")
    private Map<String, Object> globalVars;

    public EmailRequest() {
    }

    // Getters and Setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSmtpAccount() {
        return smtpAccount;
    }

    public void setSmtpAccount(String smtpAccount) {
        this.smtpAccount = smtpAccount;
    }

    public EmailRecipient getFrom() {
        return from;
    }

    public void setFrom(EmailRecipient from) {
        this.from = from;
    }

    public List<EmailRecipient> getTo() {
        return to;
    }

    public void setTo(List<EmailRecipient> to) {
        this.to = to;
    }

    public EmailContent getContent() {
        return content;
    }

    public void setContent(EmailContent content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<EmailRecipient> getCc() {
        return cc;
    }

    public void setCc(List<EmailRecipient> cc) {
        this.cc = cc;
    }

    public List<EmailRecipient> getBcc() {
        return bcc;
    }

    public void setBcc(List<EmailRecipient> bcc) {
        this.bcc = bcc;
    }

    public EmailRecipient getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(EmailRecipient replyTo) {
        this.replyTo = replyTo;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getGlobalVars() {
        return globalVars;
    }

    public void setGlobalVars(Map<String, Object> globalVars) {
        this.globalVars = globalVars;
    }
}
