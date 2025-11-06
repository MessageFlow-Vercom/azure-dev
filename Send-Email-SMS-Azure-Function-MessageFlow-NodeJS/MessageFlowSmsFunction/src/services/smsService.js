const https = require('https');

/**
 * Service for sending SMS via MessageFlow API
 */
class SmsService {
  constructor() {
    this.apiUrl = 'https://api.messageflow.com/v2.1/sms';
    this.authorization = process.env.RestApiAuthorization;
    this.applicationKey = process.env.RestApiApplicationKey;
  }

  /**
   * Send SMS via MessageFlow API
   * @param {Object} smsRequest - SMS request object
   * @param {Object} context - Azure Functions invocation context
   * @returns {Promise<Object>} Response object with success status
   */
  async sendSmsAsync(smsRequest, context) {
    context.log('SmsService.sendSmsAsync called with request:', JSON.stringify(smsRequest, null, 2));

    try {
      // Validate configuration
      if (!this.authorization || !this.applicationKey) {
        throw new Error('REST API configuration is missing. Please check RestApiAuthorization and RestApiApplicationKey settings.');
      }

      context.log('Configuration found - Authorization and ApplicationKey are set');

      // Prepare request payload
      const payload = {
        sender: smsRequest.sender,
        message: smsRequest.message,
        phoneNumbers: smsRequest.phoneNumbers,
        validity: smsRequest.validity,
        scheduleTime: smsRequest.scheduleTime,
        type: smsRequest.type,
        shortLink: smsRequest.shortLink,
        webhookUrl: smsRequest.webhookUrl,
        externalId: smsRequest.externalId
      };

      const jsonPayload = JSON.stringify(payload);
      context.log('SMS API Request Payload:', jsonPayload);

      // Send request to MessageFlow API
      const response = await this._makeHttpRequest(jsonPayload, context);

      context.log('SMS API Response Status:', response.statusCode);
      context.log('SMS API Response Content:', response.body);

      // Parse and handle response
      const apiResponse = JSON.parse(response.body);

      if (apiResponse.meta?.status === 200) {
        return {
          success: true,
          message: `SMS sent successfully! Sent: ${apiResponse.meta.sentSms}, Not sent: ${apiResponse.meta.notSentSms}`,
          responseContent: response.body
        };
      } else {
        let errorMessage = 'SMS sending failed';

        if (apiResponse.errors && apiResponse.errors.length > 0) {
          errorMessage = apiResponse.errors
            .map(e => `${e.title}: ${e.message}`)
            .join('; ');
        }

        return {
          success: false,
          message: errorMessage,
          responseContent: response.body
        };
      }
    } catch (error) {
      context.error('Error sending SMS:', error);

      return {
        success: false,
        message: `Error: ${error.message}`,
        responseContent: error.toString()
      };
    }
  }

  /**
   * Make HTTPS request to MessageFlow API
   * @private
   */
  _makeHttpRequest(payload, context) {
    return new Promise((resolve, reject) => {
      const url = new URL(this.apiUrl);

      const options = {
        hostname: url.hostname,
        path: url.pathname,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(payload),
          'Authorization': this.authorization,
          'Application-Key': this.applicationKey
        }
      };

      context.log('Sending HTTP request to:', this.apiUrl);

      const req = https.request(options, (res) => {
        let body = '';

        res.on('data', (chunk) => {
          body += chunk;
        });

        res.on('end', () => {
          resolve({
            statusCode: res.statusCode,
            headers: res.headers,
            body: body
          });
        });
      });

      req.on('error', (error) => {
        context.error('HTTP request failed:', error);
        reject(new Error(`HTTP request failed: ${error.message}`));
      });

      req.write(payload);
      req.end();
    });
  }
}

module.exports = SmsService;
