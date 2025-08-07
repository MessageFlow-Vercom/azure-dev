import azure.functions as func
import logging
import json
import os
import sys

# Add current directory to Python path for local modules
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)

# Add lib directory to Python path for locally installed packages
lib_path = os.path.join(current_dir, 'lib')
if lib_path not in sys.path:
    sys.path.insert(0, lib_path)

try:
    from email_service import EmailService, EmailRequest, EmailResponse, EmailRecipient, EmailContent
    logging.info("Successfully imported email_service module")
except ImportError as e:
    logging.error(f"Failed to import email_service: {e}")
    EmailService = None
    EmailRequest = None
    EmailResponse = None
    EmailRecipient = None
    EmailContent = None

app = func.FunctionApp()

# Load configuration
def load_config():
    """Load configuration from environment variables, local settings, or config file"""
    
    # First try environment variables (for Azure deployment)
    env_config = {
        'rest_api': {
            'authorization': os.environ.get('RestApi__Authorization'),
            'application_key': os.environ.get('RestApi__ApplicationKey')
        }
    }
    
    # Check if environment variables are set
    if env_config['rest_api']['authorization'] and env_config['rest_api']['application_key']:
        logging.info("Configuration loaded from environment variables")
        return env_config
    
    # Try local.settings.json (for local development)
    try:
        with open('local.settings.json', 'r') as f:
            settings = json.load(f)
            values = settings.get('Values', {})
            local_config = {
                'rest_api': {
                    'authorization': values.get('RestApi__Authorization'),
                    'application_key': values.get('RestApi__ApplicationKey')
                }
            }
            if local_config['rest_api']['authorization'] and local_config['rest_api']['application_key']:
                logging.info("Configuration loaded from local.settings.json")
                return local_config
    except FileNotFoundError:
        pass
    
    # Finally try config.json (fallback for deployment)
    try:
        config_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'config.json')
        with open(config_path, 'r') as f:
            file_config = json.load(f)
            if file_config.get('rest_api', {}).get('authorization') and file_config.get('rest_api', {}).get('application_key'):
                logging.info("Configuration loaded from config.json")
                return file_config
    except FileNotFoundError:
        pass
    
    # No valid configuration found
    logging.error("No valid configuration found in environment variables, local.settings.json, or config.json")
    return {
        'rest_api': {
            'authorization': None,
            'application_key': None
        }
    }

# Initialize service
config = load_config()
email_service = None

if EmailService is not None:
    try:
        email_service = EmailService(config)
        logging.info("Email service initialized successfully")
    except ValueError as e:
        logging.error(f"Failed to initialize Email service: {e}")
        email_service = None
    except Exception as e:
        logging.error(f"Unexpected error initializing Email service: {e}")
        email_service = None
else:
    logging.error("EmailService class not available - import failed")

@app.route(route="MessageFlowEmailHttpTrigger", methods=["POST"], auth_level=func.AuthLevel.ANONYMOUS)
def MessageFlowEmailHttpTrigger(req: func.HttpRequest) -> func.HttpResponse:
    logging.info('MessageFlow Email HTTP trigger function processed a request.')

    if not email_service:
        return func.HttpResponse(
            json.dumps({
                'success': False,
                'message': 'Email service is not available. Please check configuration.'
            }),
            status_code=500,
            mimetype="application/json"
        )

    try:
        # Get JSON data from request
        try:
            req_body = req.get_json()
        except ValueError:
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Invalid JSON data in request body'
                }),
                status_code=400,
                mimetype="application/json"
            )

        if not req_body:
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Request body is empty'
                }),
                status_code=400,
                mimetype="application/json"
            )

        logging.info(f"Received Email request data: {json.dumps(req_body, indent=2)}")

        # Validate required fields
        if not req_body.get('subject'):
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Missing required field: subject'
                }),
                status_code=400,
                mimetype="application/json"
            )

        if not req_body.get('smtpAccount'):
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Missing required field: smtpAccount'
                }),
                status_code=400,
                mimetype="application/json"
            )

        if not req_body.get('from'):
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Missing required field: from'
                }),
                status_code=400,
                mimetype="application/json"
            )

        to_recipients = req_body.get('to', [])
        if not to_recipients or len(to_recipients) == 0:
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Missing required field: to (must be a non-empty array)'
                }),
                status_code=400,
                mimetype="application/json"
            )

        if not req_body.get('content'):
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Missing required field: content'
                }),
                status_code=400,
                mimetype="application/json"
            )

        logging.info(f"To recipients received: {to_recipients}")

        # Create Email request
        from_recipient = EmailRecipient(
            email=req_body['from']['email'],
            name=req_body['from'].get('name'),
            message_id=req_body['from'].get('messageId'),
            vars=req_body['from'].get('vars')
        )

        to_recipients_list = [
            EmailRecipient(
                email=recipient['email'],
                name=recipient.get('name'),
                message_id=recipient.get('messageId'),
                vars=recipient.get('vars')
            ) for recipient in to_recipients
        ]

        cc_recipients_list = None
        if req_body.get('cc'):
            cc_recipients_list = [
                EmailRecipient(
                    email=recipient['email'],
                    name=recipient.get('name'),
                    message_id=recipient.get('messageId'),
                    vars=recipient.get('vars')
                ) for recipient in req_body['cc']
            ]

        bcc_recipients_list = None
        if req_body.get('bcc'):
            bcc_recipients_list = [
                EmailRecipient(
                    email=recipient['email'],
                    name=recipient.get('name'),
                    message_id=recipient.get('messageId'),
                    vars=recipient.get('vars')
                ) for recipient in req_body['bcc']
            ]

        reply_to = None
        if req_body.get('replyTo'):
            reply_to = EmailRecipient(
                email=req_body['replyTo']['email'],
                name=req_body['replyTo'].get('name'),
                message_id=req_body['replyTo'].get('messageId'),
                vars=req_body['replyTo'].get('vars')
            )

        content = EmailContent(
            html=req_body['content'].get('html'),
            text=req_body['content'].get('text'),
            template_id=req_body['content'].get('templateId')
        )

        email_request = EmailRequest(
            subject=req_body['subject'],
            smtp_account=req_body['smtpAccount'],
            from_recipient=from_recipient,
            to_recipients=to_recipients_list,
            content=content,
            tags=req_body.get('tags'),
            cc_recipients=cc_recipients_list,
            bcc_recipients=bcc_recipients_list,
            reply_to=reply_to,
            headers=req_body.get('headers'),
            global_vars=req_body.get('globalVars')
        )

        logging.info(f"Created Email request: subject={email_request.subject}, from={email_request.from_recipient.email}, to={[r.email for r in email_request.to_recipients]}")

        # Send Email
        response = email_service.send_email(email_request)

        return func.HttpResponse(
            json.dumps({
                'success': response.success,
                'message': response.message,
                'response_content': response.response_content
            }),
            status_code=200 if response.success else 500,
            mimetype="application/json"
        )

    except Exception as e:
        logging.error(f"Error processing Email request: {e}")
        return func.HttpResponse(
            json.dumps({
                'success': False,
                'message': f'An error occurred: {str(e)}'
            }),
            status_code=500,
            mimetype="application/json"
        )