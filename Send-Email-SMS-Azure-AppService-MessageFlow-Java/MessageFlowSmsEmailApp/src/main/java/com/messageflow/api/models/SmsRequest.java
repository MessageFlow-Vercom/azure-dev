package com.messageflow.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SmsRequest {

    private String sender;
    private String message;

    @JsonProperty("phoneNumbers")
    private List<String> phoneNumbers;

    private Integer validity;

    @JsonProperty("scheduleTime")
    private Long scheduleTime;

    private Integer type;

    @JsonProperty("shortLink")
    private Boolean shortLink;

    @JsonProperty("webhookUrl")
    private String webhookUrl;

    @JsonProperty("externalId")
    private String externalId;

    // Constructors
    public SmsRequest() {
    }

    public SmsRequest(String sender, String message, List<String> phoneNumbers) {
        this.sender = sender;
        this.message = message;
        this.phoneNumbers = phoneNumbers;
    }

    // Getters and Setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public Integer getValidity() {
        return validity;
    }

    public void setValidity(Integer validity) {
        this.validity = validity;
    }

    public Long getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getShortLink() {
        return shortLink;
    }

    public void setShortLink(Boolean shortLink) {
        this.shortLink = shortLink;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
