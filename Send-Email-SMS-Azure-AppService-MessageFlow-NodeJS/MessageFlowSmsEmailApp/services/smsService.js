const axios = require('axios');

class SmsRequest {
    constructor ({
        sender,
        message,
        phoneNumbers,
        validity = null,
        scheduleTime = null,
        type = null,
        shortLink = null,
        webhookUrl = null,
        externalId = null
    }) {
        this.sender = sender;
        this.message = message;
        this.phoneNumbers = phoneNumbers;
        this.validity = validity;
        this.scheduleTime = scheduleTime;
        this.type = type;
        this.shortLink = shortLink;
        this.webhookUrl = webhookUrl;
        this.externalId = externalId;
    }
}

class SmsResponse {
    constructor (success, message, responseContent = '') {
        this.success = success;
        this.message = message;
        this.responseContent = responseContent;
    }
}

class SmsService {
    constructor (config) {
        if (!config?.rest_api?.authorization) {
            throw new Error('REST API authorization is missing in configuration');
        }
        if (!config?.rest_api?.application_key) {
            throw new Error('REST API application key is missing in configuration');
        }

        this.config = config;
        this.logger = console;
    }

    async sendSms(request) {
        this.logger.info(`SmsService.sendSms called with request: ${JSON.stringify(request, null, 2)}`);

        try {
            // Prepare headers
            const headers = {
                'Authorization': this.config.rest_api.authorization,
                'Application-Key': this.config.rest_api.application_key,
                'Content-Type': 'application/json'
            };

            this.logger.info(`Headers set - Authorization: ${this.config.rest_api.authorization.substring(0, 10)}..., Application-Key: ${this.config.rest_api.application_key.substring(0, 10)}...`);

            // Prepare payload
            const payload = {
                sender: request.sender,
                message: request.message,
                phoneNumbers: request.phoneNumbers,
                validity: request.validity,
                scheduleTime: request.scheduleTime,
                type: request.type,
                shortLink: request.shortLink,
                webhookUrl: request.webhookUrl,
                externalId: request.externalId
            };

            // Remove null/undefined values
            Object.keys(payload).forEach(key => {
                if (payload[key] === null || payload[key] === undefined) {
                    delete payload[key];
                }
            });

            this.logger.info(`SMS API Request Payload: ${JSON.stringify(payload, null, 2)}`);
            this.logger.info('Sending HTTP request to: https://api.messageflow.com/v2.1/sms');

            // Send request
            const response = await axios.post(
                'https://api.messageflow.com/v2.1/sms',
                payload,
                {
                    headers,
                    timeout: 30000
                }
            );

            const responseContent = JSON.stringify(response.data);

            this.logger.info(`SMS API Response Status: ${response.status}`);
            this.logger.info(`SMS API Response Content: ${responseContent}`);

            // Parse response
            if (response.status === 200) {
                try {
                    if (typeof response.data === 'object') {
                        return new SmsResponse(
                            true,
                            response.data.message || 'SMS sent successfully',
                            responseContent
                        );
                    }
                } catch (error) {
                    this.logger.warn(`Failed to parse response to SmsResponse model, but HTTP status is success: ${error.message}`);
                }

                return new SmsResponse(
                    true,
                    'SMS sent successfully',
                    responseContent
                );
            } else {
                return new SmsResponse(
                    false,
                    `HTTP Error: ${response.status}`,
                    responseContent
                );
            }

        } catch (error) {
            if (error.response) {
                // HTTP error with response
                this.logger.error(`HTTP request failed: ${error.message}`);

                return new SmsResponse(
                    false,
                    `HTTP Error: ${error.response.status}`,
                    JSON.stringify(error.response.data)
                );
            } else if (error.request) {
                // Request made but no response
                this.logger.error(`HTTP request failed: No response received - ${error.message}`);
                return new SmsResponse(
                    false,
                    `HTTP request failed: ${error.message}`,
                    error.message
                );
            } else {
                // Error setting up request
                this.logger.error(`Error sending SMS: ${error.message}`);
                return new SmsResponse(
                    false,
                    `Error: ${error.message}`,
                    error.message
                );
            }
        }
    }
}

module.exports = {
    SmsService,
    SmsRequest,
    SmsResponse
};
