<?php

/**
 * MessageFlow SMS/Email API Application
 * Main entry point for the PHP web application
 */

// Detect if running in Azure App Service
define('IS_AZURE', getenv('WEBSITE_SITE_NAME') !== false);

// Enable error reporting [only in development]
if (!IS_AZURE) {
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
} else {
    error_reporting(E_ALL);
    ini_set('display_errors', 0);
    ini_set('log_errors', 1);
}

header('Content-Type: application/json');

// Load configuration
function loadConfig()
{
    $envAuth = getenv('Authorization') ?: ($_ENV['Authorization'] ?? null);
    $envAppKey = getenv('ApplicationKey') ?: ($_ENV['ApplicationKey'] ?? null);

    if ($envAuth && $envAppKey) {
        error_log("Using configuration from Azure Application Settings");
        return [
            'rest_api' => [
                'authorization' => $envAuth,
                'application_key' => $envAppKey
            ]
        ];
    }

    $configPath = __DIR__ . '/config.json';

    if (!file_exists($configPath)) {
        error_log("Warning: config.json not found and Azure Application Settings not available");
        return [];
    }

    $configContent = file_get_contents($configPath);
    $config = json_decode($configContent, true);

    if (json_last_error() !== JSON_ERROR_NONE) {
        error_log("Error parsing config.json: " . json_last_error_msg());
        return [];
    }

    error_log("Using configuration from config.json (local development)");
    return $config;
}

require_once __DIR__ . '/vendor/autoload.php';

use MessageFlow\SmsService;
use MessageFlow\EmailService;

$config = loadConfig();

// Validate configuration is available
if (empty($config) || !isset($config['rest_api'])) {
    error_log("CRITICAL: No valid configuration available. Check Azure Application Settings or config.json");
    http_response_code(503);
    echo json_encode([
        'success' => false,
        'message' => 'Service configuration unavailable',
        'environment' => IS_AZURE ? 'Azure App Service' : 'Local Development'
    ]);
    exit;
}

// Health message
$healthMessage = "MessageFlow SMS/Email API app is running. Use POST /sms or POST /email endpoints.";

$requestMethod = $_SERVER['REQUEST_METHOD'];
$requestUri = $_SERVER['REQUEST_URI'];
$parsedUrl = parse_url($requestUri);
$path = $parsedUrl['path'];

$scriptName = $_SERVER['SCRIPT_NAME'];
$basePath = dirname($scriptName);
if ($basePath !== '/') {
    $path = str_replace($basePath, '', $path);
}

if ($path === '/' || $path === '/index.php') {
    // Health check endpoint
    http_response_code(200);
    echo json_encode([
        'message' => $healthMessage,
        'environment' => IS_AZURE ? 'Azure App Service' : 'Local Development',
        'php_version' => PHP_VERSION,
        'timestamp' => date('c')
    ]);
    exit;
}

if ($path === '/sms' && $requestMethod === 'POST') {
    // SMS endpoint
    try {
        // Initialize SMS service
        $smsService = new SmsService($config);

        // Get JSON data from request
        $inputData = file_get_contents('php://input');
        $data = json_decode($inputData, true);

        if (json_last_error() !== JSON_ERROR_NONE || !$data) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Invalid JSON data in request body'
            ]);
            exit;
        }

        error_log("Received SMS request data: " . json_encode($data, JSON_PRETTY_PRINT));

        // Validate required fields
        if (!isset($data['sender']) || empty($data['sender'])) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Missing required field: sender'
            ]);
            exit;
        }

        if (!isset($data['message']) || empty($data['message'])) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Missing required field: message'
            ]);
            exit;
        }

        $phoneNumbers = $data['phoneNumbers'] ?? [];
        if (!is_array($phoneNumbers) || count($phoneNumbers) === 0) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Missing required field: phoneNumbers (must be a non-empty array)'
            ]);
            exit;
        }

        error_log("Phone numbers received: " . json_encode($phoneNumbers));

        // Create SMS request
        $smsRequest = [
            'sender' => $data['sender'],
            'message' => $data['message'],
            'phoneNumbers' => $phoneNumbers,
            'validity' => $data['validity'] ?? null,
            'scheduleTime' => $data['scheduleTime'] ?? null,
            'type' => $data['type'] ?? null,
            'shortLink' => $data['shortLink'] ?? null,
            'webhookUrl' => $data['webhookUrl'] ?? null,
            'externalId' => $data['externalId'] ?? null
        ];

        error_log("Created SMS request: " . json_encode($smsRequest, JSON_PRETTY_PRINT));

        // Send SMS
        $response = $smsService->sendSms($smsRequest);

        http_response_code($response['success'] ? 200 : 500);
        echo json_encode($response);
        exit;
    } catch (Exception $e) {
        error_log("Error processing SMS request: " . $e->getMessage());
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'An error occurred: ' . $e->getMessage()
        ]);
        exit;
    }
}

if ($path === '/email' && $requestMethod === 'POST') {
    // Email endpoint
    try {
        // Initialize Email service
        $emailService = new EmailService($config);

        // Get JSON data from request
        $inputData = file_get_contents('php://input');
        $data = json_decode($inputData, true);

        if (json_last_error() !== JSON_ERROR_NONE || !$data) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Invalid JSON data in request body'
            ]);
            exit;
        }

        error_log("Received Email request data: " . json_encode($data, JSON_PRETTY_PRINT));

        // Create email request
        $emailRequest = [
            'subject' => $data['subject'] ?? null,
            'smtpAccount' => $data['smtpAccount'] ?? null,
            'from' => $data['from'] ?? null,
            'to' => $data['to'] ?? [],
            'content' => $data['content'] ?? null,
            'tags' => $data['tags'] ?? null,
            'cc' => $data['cc'] ?? null,
            'bcc' => $data['bcc'] ?? null,
            'replyTo' => $data['replyTo'] ?? null,
            'headers' => $data['headers'] ?? null,
            'globalVars' => $data['globalVars'] ?? null,
            'attachments' => $data['attachments'] ?? null
        ];

        // Send email
        $response = $emailService->sendEmail($emailRequest);

        http_response_code($response['success'] ? 200 : 500);
        echo json_encode($response);
        exit;
    } catch (Exception $e) {
        error_log("Error processing email request: " . $e->getMessage());
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'An error occurred: ' . $e->getMessage()
        ]);
        exit;
    }
}

// 404 - Route not found
http_response_code(404);
echo json_encode([
    'success' => false,
    'message' => 'Endpoint not found. Available endpoints: GET /, POST /sms, POST /email'
]);
