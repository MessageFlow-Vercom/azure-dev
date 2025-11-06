package com.messageflow.api.controllers;

import com.messageflow.api.models.*;
import com.messageflow.api.services.EmailService;
import com.messageflow.api.services.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MessageFlowController {

    private static final Logger logger = LoggerFactory.getLogger(MessageFlowController.class);
    private static final String HEALTH_MESSAGE = "MessageFlow SMS/Email API app is running. Use POST /sms or POST /email endpoints.";

    @Autowired
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String healthCheck() {
        return HEALTH_MESSAGE;
    }

    @PostMapping("/")
    public String healthCheckPost() {
        return HEALTH_MESSAGE;
    }

    @PostMapping("/sms")
    public ResponseEntity<SmsResponse> sendSms(@RequestBody SmsRequest request) {
        try {
            logger.info("Received SMS request");

            // Validate required fields
            if (request.getSender() == null || request.getSender().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new SmsResponse(false, "Missing required field: sender"));
            }

            if (request.getMessage() == null || request.getMessage().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new SmsResponse(false, "Missing required field: message"));
            }

            if (request.getPhoneNumbers() == null || request.getPhoneNumbers().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new SmsResponse(false,
                                "Missing required field: phoneNumbers (must be a non-empty array)"));
            }

            logger.info("Phone numbers received: {}", request.getPhoneNumbers());
            logger.info("Created SMS request: sender={}, message={}, phoneNumbers={}",
                    request.getSender(), request.getMessage(), request.getPhoneNumbers());

            // Send SMS
            SmsResponse response = smsService.sendSms(request);

            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Error processing SMS request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SmsResponse(false, "An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/email")
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody EmailRequest request) {
        try {
            logger.info("Received Email request");

            // Validate required fields
            if (request.getSubject() == null || request.getSubject().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new EmailResponse(false, "Missing required field: subject"));
            }

            if (request.getSmtpAccount() == null || request.getSmtpAccount().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new EmailResponse(false, "Missing required field: smtpAccount"));
            }

            if (request.getFrom() == null || request.getFrom().getEmail() == null) {
                return ResponseEntity.badRequest()
                        .body(new EmailResponse(false, "Missing required field: from (with email)"));
            }

            if (request.getTo() == null || request.getTo().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new EmailResponse(false, "Missing required field: to (must be a non-empty array)"));
            }

            if (request.getContent() == null) {
                return ResponseEntity.badRequest()
                        .body(new EmailResponse(false, "Missing required field: content"));
            }

            logger.info("Created Email request: subject={}, from={}, to={}",
                    request.getSubject(), request.getFrom().getEmail(), request.getTo().size());

            // Send email
            EmailResponse response = emailService.sendEmail(request);

            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Error processing email request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EmailResponse(false, "An error occurred: " + e.getMessage()));
        }
    }
}
