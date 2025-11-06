const express = require('express');
const router = express.Router();
const { SmsService, SmsRequest } = require('../services/smsService');

// Initialize SMS service (will be set by app.js)
let smsService = null;

// Middleware to set service instance
router.use((req, res, next) => {
  if (!req.app.locals.smsService) {
    return res.status(500).json({
      success: false,
      message: 'SMS service is not available. Please check configuration.'
    });
  }
  smsService = req.app.locals.smsService;
  next();
});

/* POST SMS */
router.post('/', async (req, res) => {
  const logger = req.app.locals.logger;

  try {
    const data = req.body;
    if (!data || Object.keys(data).length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Invalid JSON data in request body'
      });
    }

    logger.info(`Received SMS request data: ${JSON.stringify(data, null, 2)}`);

    // Validate required fields
    if (!data.sender) {
      return res.status(400).json({
        success: false,
        message: 'Missing required field: sender'
      });
    }

    if (!data.message) {
      return res.status(400).json({
        success: false,
        message: 'Missing required field: message'
      });
    }

    const phoneNumbers = data.phoneNumbers || [];
    if (!Array.isArray(phoneNumbers) || phoneNumbers.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Missing required field: phoneNumbers (must be a non-empty array)'
      });
    }

    logger.info(`Phone numbers received: ${JSON.stringify(phoneNumbers)}`);

    // Create SMS request
    const smsRequest = new SmsRequest({
      sender: data.sender,
      message: data.message,
      phoneNumbers: phoneNumbers,
      validity: data.validity,
      scheduleTime: data.scheduleTime,
      type: data.type,
      shortLink: data.shortLink,
      webhookUrl: data.webhookUrl,
      externalId: data.externalId
    });

    logger.info(`Created SMS request: sender=${smsRequest.sender}, message=${smsRequest.message}, phoneNumbers=${JSON.stringify(smsRequest.phoneNumbers)}`);

    // Send SMS
    const response = await smsService.sendSms(smsRequest);

    return res.status(response.success ? 200 : 500).json({
      success: response.success,
      message: response.message,
      response_content: response.responseContent
    });

  } catch (error) {
    logger.error(`Error processing SMS request: ${error.message}`);
    return res.status(500).json({
      success: false,
      message: `An error occurred: ${error.message}`
    });
  }
});

module.exports = router;
