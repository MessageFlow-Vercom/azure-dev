package com.messageflow.function.utils;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class Config {
    private static final Logger logger = Logger.getLogger(Config.class.getName());
    private RestApiConfig restApi;

    public static class RestApiConfig {
        private String authorization;
        private String application_key;

        public String getAuthorization() {
            return authorization;
        }

        public void setAuthorization(String authorization) {
            this.authorization = authorization;
        }

        public String getApplicationKey() {
            return application_key;
        }

        public void setApplicationKey(String application_key) {
            this.application_key = application_key;
        }
    }

    public RestApiConfig getRestApi() {
        return restApi;
    }

    public void setRestApi(RestApiConfig restApi) {
        this.restApi = restApi;
    }

    /**
     * Load configuration from environment variables, or config file
     */
    public static Config loadConfig() {
        Config config = new Config();
        RestApiConfig restApiConfig = new RestApiConfig();

        // First try environment variables (for Azure deployment)
        String envAuthorization = System.getenv("RestApi__Authorization");
        String envApplicationKey = System.getenv("RestApi__ApplicationKey");

        if (envAuthorization != null && !envAuthorization.isEmpty() &&
                envApplicationKey != null && !envApplicationKey.isEmpty()) {
            logger.info("Configuration loaded from environment variables");
            restApiConfig.setAuthorization(envAuthorization);
            restApiConfig.setApplicationKey(envApplicationKey);
            config.setRestApi(restApiConfig);
            return config;
        }

        // Try config.json (fallback for deployment)
        try (FileReader reader = new FileReader("config.json")) {
            Gson gson = new Gson();
            Config fileConfig = gson.fromJson(reader, Config.class);
            if (fileConfig.getRestApi() != null &&
                    fileConfig.getRestApi().getAuthorization() != null &&
                    fileConfig.getRestApi().getApplicationKey() != null) {
                logger.info("Configuration loaded from config.json");
                return fileConfig;
            }
        } catch (IOException e) {
            logger.warning("Could not load config.json: " + e.getMessage());
        }

        // No valid configuration found
        logger.severe("No valid configuration found in environment variables or config.json");
        restApiConfig.setAuthorization(null);
        restApiConfig.setApplicationKey(null);
        config.setRestApi(restApiConfig);
        return config;
    }
}
