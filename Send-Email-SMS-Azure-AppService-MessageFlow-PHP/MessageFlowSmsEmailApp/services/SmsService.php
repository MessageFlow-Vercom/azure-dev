<?php

namespace MessageFlow;

use Exception;

/**
 * SMS Service for MessageFlow API
 */
class SmsService
{
    private $config;

    public function __construct($config)
    {
        $this->config = $config;

        // Validate configuration
        if (!isset($config['rest_api']['authorization']) || empty($config['rest_api']['authorization'])) {
            throw new Exception("REST API authorization is missing in configuration");
        }
        if (!isset($config['rest_api']['application_key']) || empty($config['rest_api']['application_key'])) {
            throw new Exception("REST API application key is missing in configuration");
        }
    }

    public function sendSms($request)
    {
        error_log("SmsService.sendSms called with request: " . json_encode($request, JSON_PRETTY_PRINT));

        try {
            // Prepare headers
            $headers = [
                'Authorization: ' . $this->config['rest_api']['authorization'],
                'Application-Key: ' . $this->config['rest_api']['application_key'],
                'Content-Type: application/json'
            ];

            $authPrefix = substr($this->config['rest_api']['authorization'], 0, 10);
            $appKeyPrefix = substr($this->config['rest_api']['application_key'], 0, 10);
            error_log("Headers set - Authorization: {$authPrefix}..., Application-Key: {$appKeyPrefix}...");

            // Prepare payload
            $payload = [
                'sender' => $request['sender'],
                'message' => $request['message'],
                'phoneNumbers' => $request['phoneNumbers']
            ];

            // Add optional fields if present
            if (isset($request['validity']) && $request['validity'] !== null) {
                $payload['validity'] = $request['validity'];
            }
            if (isset($request['scheduleTime']) && $request['scheduleTime'] !== null) {
                $payload['scheduleTime'] = $request['scheduleTime'];
            }
            if (isset($request['type']) && $request['type'] !== null) {
                $payload['type'] = $request['type'];
            }
            if (isset($request['shortLink']) && $request['shortLink'] !== null) {
                $payload['shortLink'] = $request['shortLink'];
            }
            if (isset($request['webhookUrl']) && $request['webhookUrl'] !== null) {
                $payload['webhookUrl'] = $request['webhookUrl'];
            }
            if (isset($request['externalId']) && $request['externalId'] !== null) {
                $payload['externalId'] = $request['externalId'];
            }

            $jsonPayload = json_encode($payload, JSON_PRETTY_PRINT);
            error_log("SMS API Request Payload: " . $jsonPayload);

            // Send request using cURL
            error_log("Sending HTTP request to: https://api.messageflow.com/v2.1/sms");

            $ch = curl_init('https://api.messageflow.com/v2.1/sms');
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
            curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
            curl_setopt($ch, CURLOPT_TIMEOUT, 30);

            $responseContent = curl_exec($ch);
            $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
            $curlError = curl_error($ch);
            curl_close($ch);

            error_log("SMS API Response Status: " . $httpCode);
            error_log("SMS API Response Content: " . $responseContent);

            // Handle cURL errors
            if ($curlError) {
                error_log("HTTP request failed: " . $curlError);
                return [
                    'success' => false,
                    'message' => 'HTTP request failed: ' . $curlError,
                    'response_content' => $curlError
                ];
            }

            // Parse response
            if ($httpCode == 200) {
                $responseData = json_decode($responseContent, true);

                if (json_last_error() === JSON_ERROR_NONE && is_array($responseData)) {
                    return [
                        'success' => true,
                        'message' => $responseData['message'] ?? 'SMS sent successfully',
                        'response_content' => $responseContent
                    ];
                } else {
                    // If parsing failed but HTTP status is success
                    error_log("Failed to parse response, but HTTP status is success");
                    return [
                        'success' => true,
                        'message' => 'SMS sent successfully',
                        'response_content' => $responseContent
                    ];
                }
            } else {
                return [
                    'success' => false,
                    'message' => 'HTTP Error: ' . $httpCode,
                    'response_content' => $responseContent
                ];
            }
        } catch (Exception $ex) {
            error_log("Error sending SMS: " . $ex->getMessage());
            return [
                'success' => false,
                'message' => 'Error: ' . $ex->getMessage(),
                'response_content' => $ex->getMessage()
            ];
        }
    }
}
