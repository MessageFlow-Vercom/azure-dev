package com.messageflow.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class EmailRequest {

    private String subject;

    @JsonProperty("smtpAccount")
    private String smtpAccount;

    @JsonProperty("from")
    private EmailRecipient from;

    private List<EmailRecipient> to;

    private EmailContent content;

    private List<String> tags;

    private List<EmailRecipient> cc;

    private List<EmailRecipient> bcc;

    @JsonProperty("replyTo")
    private EmailRecipient replyTo;

    private Map<String, String> headers;

    @JsonProperty("globalVars")
    private Map<String, Object> globalVars;

    private List<Map<String, Object>> attachments;

    // Constructors
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

    public List<Map<String, Object>> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Map<String, Object>> attachments) {
        this.attachments = attachments;
    }
}
