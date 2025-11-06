const { app } = require('@azure/functions');
const SmsService = require('../services/smsService');

// Create SmsService instance (reused across invocations)
const smsService = new SmsService();

/**
 * Azure Function for sending SMS via MessageFlow API
 * HTTP POST endpoint that accepts SMS data and sends it via MessageFlow
 */
app.http('MessageFlowSmsHttpTrigger', {
  methods: ['POST'],
  authLevel: 'anonymous',
  handler: async (request, context) => {
    context.log('HTTP trigger function processed a SMS request.');

    try {
      // Parse request body
      const requestBody = await request.text();

      if (!requestBody || requestBody.trim() === '') {
        return {
          status: 400,
          body: JSON.stringify({ error: 'Request body is empty' }),
          headers: { 'Content-Type': 'application/json' }
        };
      }

      // Parse JSON
      let smsRequest;
      try {
        smsRequest = JSON.parse(requestBody);
      } catch (parseError) {
        context.error('Failed to parse request JSON:', parseError);
        return {
          status: 400,
          body: JSON.stringify({ error: 'Invalid JSON format in request body' }),
          headers: { 'Content-Type': 'application/json' }
        };
      }

      // Validate required fields
      if (!smsRequest.sender ||
          !smsRequest.message ||
          !smsRequest.phoneNumbers ||
          !Array.isArray(smsRequest.phoneNumbers) ||
          smsRequest.phoneNumbers.length === 0) {
        return {
          status: 400,
          body: JSON.stringify({
            error: 'Missing required fields: sender, message, and phoneNumbers are required'
          }),
          headers: { 'Content-Type': 'application/json' }
        };
      }

      context.log(`Sending SMS to ${smsRequest.phoneNumbers.length} numbers from ${smsRequest.sender}`);

      // Send SMS using SmsService
      const result = await smsService.sendSmsAsync(smsRequest, context);

      if (result.success) {
        context.log('SMS sent successfully:', result.message);
        return {
          status: 200,
          body: JSON.stringify({
            success: true,
            message: result.message,
            responseContent: result.responseContent
          }),
          headers: { 'Content-Type': 'application/json' }
        };
      } else {
        context.error('Failed to send SMS:', result.message);
        return {
          status: 500,
          body: JSON.stringify({
            success: false,
            message: result.message,
            responseContent: result.responseContent
          }),
          headers: { 'Content-Type': 'application/json' }
        };
      }
    } catch (error) {
      context.error('Unexpected error occurred while processing SMS request:', error);
      return {
        status: 500,
        body: JSON.stringify({
          success: false,
          message: 'An unexpected error occurred',
          error: error.message
        }),
        headers: { 'Content-Type': 'application/json' }
      };
    }
  }
});
