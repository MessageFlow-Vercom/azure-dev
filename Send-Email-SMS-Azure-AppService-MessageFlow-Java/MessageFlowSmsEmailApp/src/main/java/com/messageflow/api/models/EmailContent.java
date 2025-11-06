package com.messageflow.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailContent {

    private String html;
    private String text;

    @JsonProperty("templateId")
    private String templateId;

    // Constructors
    public EmailContent() {
    }

    public EmailContent(String html, String text) {
        this.html = html;
        this.text = text;
    }

    // Getters and Setters
    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}
