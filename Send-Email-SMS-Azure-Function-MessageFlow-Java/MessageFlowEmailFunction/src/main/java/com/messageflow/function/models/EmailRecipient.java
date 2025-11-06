package com.messageflow.function.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class EmailRecipient {
    private String email;
    private String name;

    @SerializedName("messageId")
    private String messageId;

    private Map<String, Object> vars;

    public EmailRecipient() {
    }

    public EmailRecipient(String email, String name, String messageId, Map<String, Object> vars) {
        this.email = email;
        this.name = name;
        this.messageId = messageId;
        this.vars = vars;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public void setVars(Map<String, Object> vars) {
        this.vars = vars;
    }
}
