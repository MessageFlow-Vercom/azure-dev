package com.messageflow.function;

import com.google.gson.Gson;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.messageflow.function.models.SmsRequest;
import com.messageflow.function.models.SmsResponse;
import com.messageflow.function.services.SmsService;
import com.messageflow.function.utils.Config;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger for sending SMS via MessageFlow API
 */
public class SmsFunction {
    private static SmsService smsService;
    private static final Gson gson = new Gson();

    static {
        try {
            Config config = Config.loadConfig();
            smsService = new SmsService(config);
        } catch (Exception e) {
            System.err.println("Failed to initialize SMS service: " + e.getMessage());
        }
    }

    /**
     * This function listens at endpoint "/api/MessageFlowSmsHttpTrigger".
     */
    @FunctionName("MessageFlowSmsHttpTrigger")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("MessageFlow SMS HTTP trigger function processed a request.");

        if (smsService == null) {
            SmsResponse errorResponse = new SmsResponse(
                    false,
                    "SMS service is not available. Please check configuration.",
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
                SmsResponse errorResponse = new SmsResponse(
                        false,
                        "Request body is empty",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            context.getLogger().info("Received SMS request data: " + body);

            // Parse request
            SmsRequest smsRequest = gson.fromJson(body, SmsRequest.class);

            // Validate required fields
            if (smsRequest.getSender() == null || smsRequest.getSender().isEmpty()) {
                SmsResponse errorResponse = new SmsResponse(
                        false,
                        "Missing required field: sender",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            if (smsRequest.getMessage() == null || smsRequest.getMessage().isEmpty()) {
                SmsResponse errorResponse = new SmsResponse(
                        false,
                        "Missing required field: message",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            if (smsRequest.getPhoneNumbers() == null || smsRequest.getPhoneNumbers().isEmpty()) {
                SmsResponse errorResponse = new SmsResponse(
                        false,
                        "Missing required field: phoneNumbers (must be a non-empty array)",
                        ""
                );
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(errorResponse))
                        .build();
            }

            context.getLogger().info("Created SMS request: sender=" + smsRequest.getSender() +
                    ", message=" + smsRequest.getMessage() +
                    ", phoneNumbers=" + String.join(", ", smsRequest.getPhoneNumbers()));

            // Send SMS
            SmsResponse response = smsService.sendSms(smsRequest);

            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

            return request.createResponseBuilder(status)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(response))
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error processing SMS request: " + e.getMessage());
            SmsResponse errorResponse = new SmsResponse(
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
