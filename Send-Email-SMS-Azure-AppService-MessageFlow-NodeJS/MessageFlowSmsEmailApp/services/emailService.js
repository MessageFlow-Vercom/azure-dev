const axios = require('axios');

class EmailRecipient {
    constructor ({ email, name = null, messageId = null, vars = null }) {
        this.email = email;
        this.name = name;
        this.messageId = messageId;
        this.vars = vars;
    }

    toJSON() {
        const obj = { email: this.email };
        if (this.name !== null) obj.name = this.name;
        if (this.messageId !== null) obj.messageId = this.messageId;
        if (this.vars !== null) obj.vars = this.vars;
        return obj;
    }
}

class EmailContent {
    constructor ({ html = null, text = null, templateId = null }) {
        this.html = html;
        this.text = text;
        this.templateId = templateId;
    }

    toJSON() {
        const obj = {};
        if (this.html !== null) obj.html = this.html;
        if (this.text !== null) obj.text = this.text;
        if (this.templateId !== null) obj.templateId = this.templateId;
        return obj;
    }
}

class EmailAttachment {
    constructor ({ fileName, fileMime, fileContent, inline = false }) {
        this.fileName = fileName;
        this.fileMime = fileMime;
        this.fileContent = fileContent;
        this.inline = inline;
    }

    toJSON() {
        return {
            fileName: this.fileName,
            fileMime: this.fileMime,
            fileContent: this.fileContent,
            inline: this.inline
        };
    }
}

class EmailRequest {
    constructor ({
        subject,
        smtpAccount,
        from,
        to,
        content,
        tags = null,
        cc = null,
        bcc = null,
        replyTo = null,
        headers = null,
        globalVars = null,
        attachments = null
    }) {
        this.subject = subject;
        this.smtpAccount = smtpAccount;
        this.from = from;
        this.to = to;
        this.content = content;
        this.tags = tags;
        this.cc = cc;
        this.bcc = bcc;
        this.replyTo = replyTo;
        this.headers = headers;
        this.globalVars = globalVars;
        this.attachments = attachments;
    }
}

class EmailResponse {
    constructor (success, message, responseContent = '') {
        this.success = success;
        this.message = message;
        this.responseContent = responseContent;
    }
}

class EmailService {
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

    async sendEmail(request) {
        this.logger.info(`EmailService.sendEmail called with request: ${JSON.stringify(request, null, 2)}`);

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
                subject: request.subject,
                smtpAccount: request.smtpAccount,
                tags: request.tags,
                content: request.content.toJSON ? request.content.toJSON() : request.content,
                bcc: request.bcc ? request.bcc.map(r => r.toJSON ? r.toJSON() : r) : undefined,
                cc: request.cc ? request.cc.map(r => r.toJSON ? r.toJSON() : r) : undefined,
                from: request.from.toJSON ? request.from.toJSON() : request.from,
                replyTo: request.replyTo ? (request.replyTo.toJSON ? request.replyTo.toJSON() : request.replyTo) : undefined,
                headers: request.headers,
                globalVars: request.globalVars,
                to: request.to.map(r => r.toJSON ? r.toJSON() : r),
                attachments: request.attachments ? request.attachments.map(a => a.toJSON ? a.toJSON() : a) : undefined
            };

            // Remove undefined values
            Object.keys(payload).forEach(key => {
                if (payload[key] === undefined) {
                    delete payload[key];
                }
            });

            this.logger.info(`Email API Request Payload: ${JSON.stringify(payload, null, 2)}`);
            this.logger.info('Sending HTTP request to: https://api.messageflow.com/v2.1/email');

            // Send request
            const response = await axios.post(
                'https://api.messageflow.com/v2.1/email',
                payload,
                {
                    headers,
                    timeout: 30000
                }
            );

            const responseContent = JSON.stringify(response.data);

            this.logger.info(`Email API Response Status: ${response.status}`);
            this.logger.info(`Email API Response Content: ${responseContent}`);

            // Parse response
            if (response.status === 200) {
                try {
                    if (typeof response.data === 'object') {
                        return new EmailResponse(
                            true,
                            response.data.message || 'Email sent successfully',
                            responseContent
                        );
                    }
                } catch (error) {
                    this.logger.warn(`Failed to parse response to EmailResponse model, but HTTP status is success: ${error.message}`);
                }

                return new EmailResponse(
                    true,
                    'Email sent successfully',
                    responseContent
                );
            } else {
                return new EmailResponse(
                    false,
                    `HTTP Error: ${response.status}`,
                    responseContent
                );
            }

        } catch (error) {
            if (error.response) {
                // HTTP error with response
                this.logger.error(`HTTP request failed: ${error.message}`);
                return new EmailResponse(
                    false,
                    `HTTP Error: ${error.response.status}`,
                    JSON.stringify(error.response.data)
                );
            } else if (error.request) {
                // Request made but no response
                this.logger.error(`HTTP request failed: No response received - ${error.message}`);
                return new EmailResponse(
                    false,
                    `HTTP request failed: ${error.message}`,
                    error.message
                );
            } else {
                // Error setting up request
                this.logger.error(`Error sending email: ${error.message}`);
                return new EmailResponse(
                    false,
                    `Error: ${error.message}`,
                    error.message
                );
            }
        }
    }
}

module.exports = {
    EmailService,
    EmailRequest,
    EmailResponse,
    EmailRecipient,
    EmailContent,
    EmailAttachment
};
