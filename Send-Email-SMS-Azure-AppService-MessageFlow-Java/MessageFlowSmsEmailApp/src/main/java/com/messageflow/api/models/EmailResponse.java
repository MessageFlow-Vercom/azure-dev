package com.messageflow.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailResponse {

    private boolean success;
    private String message;

    @JsonProperty("response_content")
    private String responseContent;

    // Constructors
    public EmailResponse() {
    }

    public EmailResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.responseContent = "";
    }

    public EmailResponse(boolean success, String message, String responseContent) {
        this.success = success;
        this.message = message;
        this.responseContent = responseContent;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }
}
