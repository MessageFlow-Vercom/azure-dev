<?php

namespace MessageFlow;

use Exception;

/**
 * Email Service for MessageFlow API
 */
class EmailService
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

    public function sendEmail($request)
    {
        error_log("EmailService.sendEmail called with request: " . json_encode($request, JSON_PRETTY_PRINT));

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
                'subject' => $request['subject'],
                'smtpAccount' => $request['smtpAccount'],
                'from' => $request['from'],
                'to' => $request['to'],
                'content' => $request['content']
            ];

            // Add optional fields if present
            if (isset($request['tags']) && $request['tags'] !== null) {
                $payload['tags'] = $request['tags'];
            }
            if (isset($request['cc']) && $request['cc'] !== null) {
                $payload['cc'] = $request['cc'];
            }
            if (isset($request['bcc']) && $request['bcc'] !== null) {
                $payload['bcc'] = $request['bcc'];
            }
            if (isset($request['replyTo']) && $request['replyTo'] !== null) {
                $payload['replyTo'] = $request['replyTo'];
            }
            if (isset($request['headers']) && $request['headers'] !== null) {
                $payload['headers'] = $request['headers'];
            }
            if (isset($request['globalVars']) && $request['globalVars'] !== null) {
                $payload['globalVars'] = $request['globalVars'];
            }
            if (isset($request['attachments']) && $request['attachments'] !== null) {
                $payload['attachments'] = $request['attachments'];
            }

            $jsonPayload = json_encode($payload, JSON_PRETTY_PRINT);
            error_log("Email API Request Payload: " . $jsonPayload);

            // Send request using cURL
            error_log("Sending HTTP request to: https://api.messageflow.com/v2.1/email");

            $ch = curl_init('https://api.messageflow.com/v2.1/email');
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
            curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
            curl_setopt($ch, CURLOPT_TIMEOUT, 30);

            $responseContent = curl_exec($ch);
            $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
            $curlError = curl_error($ch);
            curl_close($ch);

            error_log("Email API Response Status: " . $httpCode);
            error_log("Email API Response Content: " . $responseContent);

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
                        'message' => $responseData['message'] ?? 'Email sent successfully',
                        'response_content' => $responseContent
                    ];
                } else {
                    // If parsing failed but HTTP status is success
                    error_log("Failed to parse response, but HTTP status is success");
                    return [
                        'success' => true,
                        'message' => 'Email sent successfully',
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
            error_log("Error sending email: " . $ex->getMessage());
            return [
                'success' => false,
                'message' => 'Error: ' . $ex->getMessage(),
                'response_content' => $ex->getMessage()
            ];
        }
    }
}
