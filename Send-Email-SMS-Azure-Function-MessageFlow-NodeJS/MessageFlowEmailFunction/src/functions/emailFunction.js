const { app } = require('@azure/functions');
const EmailService = require('../services/emailService');

// Create EmailService instance (reused across invocations)
const emailService = new EmailService();

/**
 * Azure Function for sending emails via MessageFlow API
 * HTTP POST endpoint that accepts email data and sends it via MessageFlow
 */
app.http('MessageFlowEmailHttpTrigger', {
  methods: ['POST'],
  authLevel: 'anonymous',
  handler: async (request, context) => {
    context.log('HTTP trigger function processed an Email request.');

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
      let emailRequest;
      try {
        emailRequest = JSON.parse(requestBody);
      } catch (parseError) {
        context.error('Failed to parse request JSON:', parseError);
        return {
          status: 400,
          body: JSON.stringify({ error: 'Invalid JSON format in request body' }),
          headers: { 'Content-Type': 'application/json' }
        };
      }

      // Validate required fields
      if (!emailRequest.subject ||
          !emailRequest.to ||
          !Array.isArray(emailRequest.to) ||
          emailRequest.to.length === 0 ||
          !emailRequest.content) {
        return {
          status: 400,
          body: JSON.stringify({
            error: 'Missing required fields: subject, to, and content are required'
          }),
          headers: { 'Content-Type': 'application/json' }
        };
      }

      context.log(`Sending Email to ${emailRequest.to.length} recipients with subject: ${emailRequest.subject}`);

      // Send Email using EmailService
      const result = await emailService.sendEmailAsync(emailRequest, context);

      if (result.success) {
        context.log('Email sent successfully:', result.message);
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
        context.error('Failed to send Email:', result.message);
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
      context.error('Unexpected error occurred while processing Email request:', error);
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
