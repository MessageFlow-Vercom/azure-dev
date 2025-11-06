const express = require('express');
const router = express.Router();
const { EmailService, EmailRequest, EmailRecipient, EmailContent } = require('../services/emailService');

// Initialize Email service (will be set by app.js)
let emailService = null;

// Middleware to set service instance
router.use((req, res, next) => {
  if (!req.app.locals.emailService) {
    return res.status(500).json({
      success: false,
      message: 'Email service is not available. Please check configuration.'
    });
  }
  emailService = req.app.locals.emailService;
  next();
});

/* POST Email */
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

    // Parse recipients
    const toRecipients = [];
    for (const recipientData of (data.to || [])) {
      toRecipients.push(new EmailRecipient({
        email: recipientData.email,
        name: recipientData.name,
        messageId: recipientData.messageId,
        vars: recipientData.vars
      }));
    }

    // Parse optional CC recipients
    let ccRecipients = null;
    if (data.cc) {
      ccRecipients = [];
      for (const recipientData of data.cc) {
        ccRecipients.push(new EmailRecipient({
          email: recipientData.email,
          name: recipientData.name,
          messageId: recipientData.messageId,
          vars: recipientData.vars
        }));
      }
    }

    // Parse optional BCC recipients
    let bccRecipients = null;
    if (data.bcc) {
      bccRecipients = [];
      for (const recipientData of data.bcc) {
        bccRecipients.push(new EmailRecipient({
          email: recipientData.email,
          name: recipientData.name,
          messageId: recipientData.messageId,
          vars: recipientData.vars
        }));
      }
    }

    // Parse from recipient
    const fromData = data.from || {};
    const fromRecipient = new EmailRecipient({
      email: fromData.email,
      name: fromData.name,
      messageId: fromData.messageId,
      vars: fromData.vars
    });

    // Parse reply-to recipient
    let replyTo = null;
    if (data.replyTo) {
      const replyToData = data.replyTo;
      replyTo = new EmailRecipient({
        email: replyToData.email,
        name: replyToData.name,
        messageId: replyToData.messageId,
        vars: replyToData.vars
      });
    }

    // Parse content
    const contentData = data.content || {};
    const content = new EmailContent({
      html: contentData.html,
      text: contentData.text,
      templateId: contentData.templateId
    });

    // Create email request
    const emailRequest = new EmailRequest({
      subject: data.subject,
      smtpAccount: data.smtpAccount,
      from: fromRecipient,
      to: toRecipients,
      content: content,
      tags: data.tags,
      cc: ccRecipients,
      bcc: bccRecipients,
      replyTo: replyTo,
      headers: data.headers,
      globalVars: data.globalVars,
      attachments: data.attachments
    });

    // Send email
    const response = await emailService.sendEmail(emailRequest);

    return res.status(response.success ? 200 : 500).json({
      success: response.success,
      message: response.message,
      response_content: response.responseContent
    });

  } catch (error) {
    logger.error(`Error processing email request: ${error.message}`);
    return res.status(500).json({
      success: false,
      message: `An error occurred: ${error.message}`
    });
  }
});

module.exports = router;
