package com.messageflow.function.models;

import com.google.gson.annotations.SerializedName;

public class EmailResponse {
    private boolean success;
    private String message;

    @SerializedName("response_content")
    private String responseContent;

    public EmailResponse() {
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
