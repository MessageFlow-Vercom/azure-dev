package com.messageflow.function;

import com.google.gson.Gson;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.messageflow.function.models.EmailRequest;
import com.messageflow.function.models.EmailResponse;
import com.messageflow.function.services.EmailService;
import com.messageflow.function.utils.Config;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger for sending emails via MessageFlow API
 */
public class EmailFunction {
    private static EmailService emailService;
    private static final Gson gson = new Gson();

    static {
        try {
            Config config = Config.loadConfig();
            emailService = new EmailService(config);
        } catch (Exception e) {
            System.err.println("Failed to initialize Email service: " + e.getMessage());
        }
    }

    /**
     * This function listens at endpoint "/api/MessageFlowEmailHttpTrigger".
     */
    @FunctionName("MessageFlowEmailHttpTrigger")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("MessageFlow Email HTTP trigger function processed a request.");

        if (emailService == null) {
            EmailResponse errorResponse = new EmailResponse(
                    false,
                    "Email service is not available. Please check configuration.",
                    ""
            );
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(errorResponse))
                    .build();
        }

        try {
            // Get JSON data from request
            String body = request.getBody().orElse(null);

            if (body == null || body.isEmpty()) {
                EmailResponse errorResponse = new EmailResponse(
                        false,
                        "Request body is empty",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            context.getLogger().info("Received Email request data: " + body);

            // Parse request
            EmailRequest emailRequest = gson.fromJson(body, EmailRequest.class);

            // Validate required fields
            if (emailRequest.getSubject() == null || emailRequest.getSubject().isEmpty()) {
                EmailResponse errorResponse = new EmailResponse(
                        false,
                        "Missing required field: subject",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            if (emailRequest.getSmtpAccount() == null || emailRequest.getSmtpAccount().isEmpty()) {
                EmailResponse errorResponse = new EmailResponse(
                        false,
                        "Missing required field: smtpAccount",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            if (emailRequest.getFrom() == null) {
                EmailResponse errorResponse = new EmailResponse(
                        false,
                        "Missing required field: from",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            if (emailRequest.getTo() == null || emailRequest.getTo().isEmpty()) {
                EmailResponse errorResponse = new EmailResponse(
                        false,
                        "Missing required field: to (must be a non-empty array)",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            if (emailRequest.getContent() == null) {
                EmailResponse errorResponse = new EmailResponse(
                        false,
                        "Missing required field: content",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            context.getLogger().info("Created Email request: subject=" + emailRequest.getSubject() +
                    ", from=" + emailRequest.getFrom().getEmail());

            // Send Email
            EmailResponse response = emailService.sendEmail(emailRequest);

            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

            return request.createResponseBuilder(status)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(response))
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error processing Email request: " + e.getMessage());
            EmailResponse errorResponse = new EmailResponse(
                    false,
                    "An error occurred: " + e.getMessage(),
                    ""
            );
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(errorResponse))
                    .build();
        }
    }
}
