package com.messageflow.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messageflow.api.models.EmailRequest;
import com.messageflow.api.models.EmailResponse;
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
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String EMAIL_API_URL = "https://api.messageflow.com/v2.1/email";

    @Value("${messageflow.api.authorization}")
    private String authorization;

    @Value("${messageflow.api.application-key}")
    private String applicationKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public EmailService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public EmailResponse sendEmail(EmailRequest request) {
        try {
            logger.info("EmailService.sendEmail called with request: {}", objectMapper.writeValueAsString(request));

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
            payload.put("subject", request.getSubject());
            payload.put("smtpAccount", request.getSmtpAccount());
            payload.put("from", request.getFrom());
            payload.put("to", request.getTo());
            payload.put("content", request.getContent());

            if (request.getTags() != null) {
                payload.put("tags", request.getTags());
            }
            if (request.getCc() != null) {
                payload.put("cc", request.getCc());
            }
            if (request.getBcc() != null) {
                payload.put("bcc", request.getBcc());
            }
            if (request.getReplyTo() != null) {
                payload.put("replyTo", request.getReplyTo());
            }
            if (request.getHeaders() != null) {
                payload.put("headers", request.getHeaders());
            }
            if (request.getGlobalVars() != null) {
                payload.put("globalVars", request.getGlobalVars());
            }
            if (request.getAttachments() != null) {
                payload.put("attachments", request.getAttachments());
            }

            String jsonPayload = objectMapper.writeValueAsString(payload);
            logger.info("Email API Request Payload: {}", jsonPayload);
            logger.info("Sending HTTP request to: {}", EMAIL_API_URL);

            // Send request
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    EMAIL_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String responseContent = response.getBody();
            logger.info("Email API Response Status: {}", response.getStatusCode());
            logger.info("Email API Response Content: {}", responseContent);

            // Parse response
            if (response.getStatusCode() == HttpStatus.OK) {
                try {
                    Map<String, Object> responseData = objectMapper.readValue(responseContent, Map.class);
                    String message = responseData.containsKey("message") ?
                            responseData.get("message").toString() : "Email sent successfully";
                    return new EmailResponse(true, message, responseContent);
                } catch (Exception e) {
                    logger.warn("Failed to parse response to EmailResponse model, but HTTP status is success: {}", e.getMessage());
                    return new EmailResponse(true, "Email sent successfully", responseContent);
                }
            } else {
                return new EmailResponse(false, "HTTP Error: " + response.getStatusCode(), responseContent);
            }

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            logger.error("HTTP request failed: {}", ex.getMessage());
            return new EmailResponse(false, "HTTP Error: " + ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            logger.error("Error sending email: {}", ex.getMessage());
            return new EmailResponse(false, "Error: " + ex.getMessage(), ex.getMessage());
        }
    }
}
