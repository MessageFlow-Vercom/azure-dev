package com.messageflow.function.models;

import com.google.gson.annotations.SerializedName;

public class EmailContent {
    private String html;
    private String text;

    @SerializedName("templateId")
    private String templateId;

    public EmailContent() {
    }

    public EmailContent(String html, String text, String templateId) {
        this.html = html;
        this.text = text;
        this.templateId = templateId;
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
