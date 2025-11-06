package com.messageflow.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class EmailRecipient {

    private String email;
    private String name;

    @JsonProperty("messageId")
    private String messageId;

    private Map<String, Object> vars;

    // Constructors
    public EmailRecipient() {
    }

    public EmailRecipient(String email) {
        this.email = email;
    }

    public EmailRecipient(String email, String name) {
        this.email = email;
        this.name = name;
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
