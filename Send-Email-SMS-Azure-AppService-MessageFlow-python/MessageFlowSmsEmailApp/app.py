import json
import logging
from flask import Flask, request, jsonify
from services.sms_service import SmsService, SmsRequest
from services.email_service import EmailService, EmailRequest, EmailRecipient, EmailContent

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Load configuration
def load_config():
    try:
        with open('config.json', 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        logger.error("config.json not found")
        return {}
    except json.JSONDecodeError as e:
        logger.error(f"Error parsing config.json: {e}")
        return {}

config = load_config()

# Initialize services
try:
    sms_service = SmsService(config)
    email_service = EmailService(config)
except ValueError as e:
    logger.error(f"Failed to initialize services: {e}")
    sms_service = None
    email_service = None

# Health message
health_message = "MessageFlow SMS/Email API app is running. Use POST /sms or POST /email endpoints."

@app.route('/', methods=['GET', 'POST'])
def health_check():
    return health_message

@app.route('/sms', methods=['POST'])
def send_sms():
    if not sms_service:
        return jsonify({
            'success': False,
            'message': 'SMS service is not available. Please check configuration.'
        }), 500
    
    try:
        # Get JSON data from request
        data = request.get_json()
        if not data:
            return jsonify({
                'success': False,
                'message': 'Invalid JSON data in request body'
            }), 400
        
        # Log received data for debugging
        logger.info(f"Received SMS request data: {json.dumps(data, indent=2)}")
        
        # Validate required fields
        if not data.get('sender'):
            return jsonify({
                'success': False,
                'message': 'Missing required field: sender'
            }), 400
            
        if not data.get('message'):
            return jsonify({
                'success': False,
                'message': 'Missing required field: message'
            }), 400
            
        phone_numbers = data.get('phoneNumbers', [])
        if not phone_numbers or len(phone_numbers) == 0:
            return jsonify({
                'success': False,
                'message': 'Missing required field: phoneNumbers (must be a non-empty array)'
            }), 400
        
        logger.info(f"Phone numbers received: {phone_numbers}")
        
        # Create SMS request
        sms_request = SmsRequest(
            sender=data.get('sender'),
            message=data.get('message'),
            phone_numbers=phone_numbers,
            validity=data.get('validity'),
            schedule_time=data.get('scheduleTime'),
            type=data.get('type'),
            short_link=data.get('shortLink'),
            webhook_url=data.get('webhookUrl'),
            external_id=data.get('externalId')
        )
        
        logger.info(f"Created SMS request: sender={sms_request.sender}, message={sms_request.message}, phone_numbers={sms_request.phone_numbers}")
        
        # Send SMS
        response = sms_service.send_sms(sms_request)
        
        return jsonify({
            'success': response.success,
            'message': response.message,
            'response_content': response.response_content
        }), 200 if response.success else 500
        
    except Exception as e:
        logger.error(f"Error processing SMS request: {e}")
        return jsonify({
            'success': False,
            'message': f'An error occurred: {str(e)}'
        }), 500

@app.route('/email', methods=['POST'])
def send_email():
    if not email_service:
        return jsonify({
            'success': False,
            'message': 'Email service is not available. Please check configuration.'
        }), 500
    
    try:
        # Get JSON data from request
        data = request.get_json()
        if not data:
            return jsonify({
                'success': False,
                'message': 'Invalid JSON data in request body'
            }), 400
        
        # Parse recipients
        to_recipients = []
        for recipient_data in data.get('to', []):
            to_recipients.append(EmailRecipient(
                email=recipient_data.get('email'),
                name=recipient_data.get('name'),
                message_id=recipient_data.get('messageId'),
                vars=recipient_data.get('vars')
            ))
        
        # Parse optional CC recipients
        cc_recipients = None
        if data.get('cc'):
            cc_recipients = []
            for recipient_data in data['cc']:
                cc_recipients.append(EmailRecipient(
                    email=recipient_data.get('email'),
                    name=recipient_data.get('name'),
                    message_id=recipient_data.get('messageId'),
                    vars=recipient_data.get('vars')
                ))
        
        # Parse optional BCC recipients
        bcc_recipients = None
        if data.get('bcc'):
            bcc_recipients = []
            for recipient_data in data['bcc']:
                bcc_recipients.append(EmailRecipient(
                    email=recipient_data.get('email'),
                    name=recipient_data.get('name'),
                    message_id=recipient_data.get('messageId'),
                    vars=recipient_data.get('vars')
                ))
        
        # Parse from recipient
        from_data = data.get('from', {})
        from_recipient = EmailRecipient(
            email=from_data.get('email'),
            name=from_data.get('name'),
            message_id=from_data.get('messageId'),
            vars=from_data.get('vars')
        )
        
        # Parse reply-to recipient
        reply_to = None
        if data.get('reply_to'):
            reply_to_data = data['reply_to']
            reply_to = EmailRecipient(
                email=reply_to_data.get('email'),
                name=reply_to_data.get('name'),
                message_id=reply_to_data.get('messageId'),
                vars=reply_to_data.get('vars')
            )
        
        # Parse content
        content_data = data.get('content', {})
        content = EmailContent(
            html=content_data.get('html'),
            text=content_data.get('text'),
            template_id=content_data.get('templateId')
        )
        
        # Create email request
        email_request = EmailRequest(
            subject=data.get('subject'),
            smtp_account=data.get('smtpAccount'),
            from_recipient=from_recipient,
            to_recipients=to_recipients,
            content=content,
            tags=data.get('tags'),
            cc_recipients=cc_recipients,
            bcc_recipients=bcc_recipients,
            reply_to=reply_to,
            headers=data.get('headers'),
            global_vars=data.get('globalVars'),
            attachments=data.get('attachments')
        )
        
        # Send email
        response = email_service.send_email(email_request)
        
        return jsonify({
            'success': response.success,
            'message': response.message,
            'response_content': response.response_content
        }), 200 if response.success else 500
        
    except Exception as e:
        logger.error(f"Error processing email request: {e}")
        return jsonify({
            'success': False,
            'message': f'An error occurred: {str(e)}'
        }), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5001) 
