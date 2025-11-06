package com.messageflow.function.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.messageflow.function.models.SmsRequest;
import com.messageflow.function.models.SmsResponse;
import com.messageflow.function.utils.Config;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SmsService {
    private static final String SMS_API_URL = "https://api.messageflow.com/v2.1/sms";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final Config config;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Logger logger;

    public SmsService(Config config) {
        if (config.getRestApi().getAuthorization() == null || config.getRestApi().getAuthorization().isEmpty()) {
            throw new IllegalArgumentException("REST API authorization is missing in configuration");
        }
        if (config.getRestApi().getApplicationKey() == null || config.getRestApi().getApplicationKey().isEmpty()) {
            throw new IllegalArgumentException("REST API application key is missing in configuration");
        }

        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder()
                .setExclusionStrategies(new NullExclusionStrategy())
                .create();
        this.logger = Logger.getLogger(SmsService.class.getName());
    }

    public SmsResponse sendSms(SmsRequest request) {
        logger.info("SmsService.sendSms called with request: " + gson.toJson(request));

        try {
            // Prepare payload
            String jsonPayload = gson.toJson(request);
            logger.info("SMS API Request Payload: " + jsonPayload);

            // Prepare request
            Request httpRequest = new Request.Builder()
                    .url(SMS_API_URL)
                    .addHeader("Authorization", config.getRestApi().getAuthorization())
                    .addHeader("Application-Key", config.getRestApi().getApplicationKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonPayload, JSON))
                    .build();

            logger.info("Sending HTTP request to: " + SMS_API_URL);

            // Send request
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseContent = response.body() != null ? response.body().string() : "";
                logger.info("SMS API Response Status: " + response.code());
                logger.info("SMS API Response Content: " + responseContent);

                if (response.isSuccessful()) {
                    return new SmsResponse(true, "SMS sent successfully", responseContent);
                } else {
                    return new SmsResponse(false, "HTTP Error: " + response.code(), responseContent);
                }
            }

        } catch (IOException e) {
            logger.severe("HTTP request failed: " + e.getMessage());
            return new SmsResponse(false, "HTTP request failed: " + e.getMessage(), e.toString());
        } catch (Exception e) {
            logger.severe("Error sending SMS: " + e.getMessage());
            return new SmsResponse(false, "Error: " + e.getMessage(), e.toString());
        }
    }
}
