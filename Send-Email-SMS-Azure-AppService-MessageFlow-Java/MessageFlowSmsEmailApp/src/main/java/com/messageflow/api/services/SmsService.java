package com.messageflow.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messageflow.api.models.SmsRequest;
import com.messageflow.api.models.SmsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private static final String SMS_API_URL = "https://api.messageflow.com/v2.1/sms";

    @Value("${messageflow.api.authorization}")
    private String authorization;

    @Value("${messageflow.api.application-key}")
    private String applicationKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SmsService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public SmsResponse sendSms(SmsRequest request) {
        try {
            logger.info("SmsService.sendSms called with request: {}", objectMapper.writeValueAsString(request));

            // Validate configuration
            if (authorization == null || authorization.isEmpty()) {
                throw new IllegalStateException("REST API authorization is missing in configuration");
            }
            if (applicationKey == null || applicationKey.isEmpty()) {
                throw new IllegalStateException("REST API application key is missing in configuration");
            }

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authorization);
            headers.set("Application-Key", applicationKey);

            logger.info("Headers set - Authorization: {}..., Application-Key: {}...",
                    authorization.substring(0, Math.min(10, authorization.length())),
                    applicationKey.substring(0, Math.min(10, applicationKey.length())));

            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", request.getSender());
            payload.put("message", request.getMessage());
            payload.put("phoneNumbers", request.getPhoneNumbers());

            if (request.getValidity() != null) {
                payload.put("validity", request.getValidity());
            }
            if (request.getScheduleTime() != null) {
                payload.put("scheduleTime", request.getScheduleTime());
            }
            if (request.getType() != null) {
                payload.put("type", request.getType());
            }
            if (request.getShortLink() != null) {
                payload.put("shortLink", request.getShortLink());
            }
            if (request.getWebhookUrl() != null) {
                payload.put("webhookUrl", request.getWebhookUrl());
            }
            if (request.getExternalId() != null) {
                payload.put("externalId", request.getExternalId());
            }

            String jsonPayload = objectMapper.writeValueAsString(payload);

            logger.info("SMS API Request Payload: {}", jsonPayload);
            logger.info("Sending HTTP request to: {}", SMS_API_URL);

            // Send request
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    SMS_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class);

            String responseContent = response.getBody();

            logger.info("SMS API Response Status: {}", response.getStatusCode());
            logger.info("SMS API Response Content: {}", responseContent);

            // Parse response
            if (response.getStatusCode() == HttpStatus.OK) {
                try {
                    Map<String, Object> responseData = objectMapper.readValue(responseContent, Map.class);
                    String message = responseData.containsKey("message") ? responseData.get("message").toString()
                            : "SMS sent successfully";
                    return new SmsResponse(true, message, responseContent);
                } catch (Exception e) {
                    logger.warn("Failed to parse response to SmsResponse model, but HTTP status is success: {}",
                            e.getMessage());
                    return new SmsResponse(true, "SMS sent successfully", responseContent);
                }
            } else {
                return new SmsResponse(false, "HTTP Error: " + response.getStatusCode(), responseContent);
            }

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            logger.error("HTTP request failed: {}", ex.getMessage());
            return new SmsResponse(false, "HTTP Error: " + ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            logger.error("Error sending SMS: {}", ex.getMessage());
            return new SmsResponse(false, "Error: " + ex.getMessage(), ex.getMessage());
        }
    }
}
