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
    from sms_service import SmsService, SmsRequest, SmsResponse
    logging.info("Successfully imported sms_service module")
except ImportError as e:
    logging.error(f"Failed to import sms_service: {e}")
    SmsService = None
    SmsRequest = None
    SmsResponse = None

app = func.FunctionApp(http_auth_level=func.AuthLevel.ANONYMOUS)

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
sms_service = None

if SmsService is not None:
    try:
        sms_service = SmsService(config)
        logging.info("SMS service initialized successfully")
    except ValueError as e:
        logging.error(f"Failed to initialize SMS service: {e}")
        sms_service = None
    except Exception as e:
        logging.error(f"Unexpected error initializing SMS service: {e}")
        sms_service = None
else:
    logging.error("SmsService class not available - import failed")

@app.route(route="MessageFlowSmsHttpTrigger", methods=["POST"])
def MessageFlowSmsHttpTrigger(req: func.HttpRequest) -> func.HttpResponse:
    logging.info('MessageFlow SMS HTTP trigger function processed a request.')

    if not sms_service:
        return func.HttpResponse(
            json.dumps({
                'success': False,
                'message': 'SMS service is not available. Please check configuration.'
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

        logging.info(f"Received SMS request data: {json.dumps(req_body, indent=2)}")

        # Validate required fields
        if not req_body.get('sender'):
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Missing required field: sender'
                }),
                status_code=400,
                mimetype="application/json"
            )

        if not req_body.get('message'):
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Missing required field: message'
                }),
                status_code=400,
                mimetype="application/json"
            )

        phone_numbers = req_body.get('phoneNumbers', [])
        if not phone_numbers or len(phone_numbers) == 0:
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'Missing required field: phoneNumbers (must be a non-empty array)'
                }),
                status_code=400,
                mimetype="application/json"
            )

        logging.info(f"Phone numbers received: {phone_numbers}")

        # Check if SmsRequest class is available
        if SmsRequest is None:
            return func.HttpResponse(
                json.dumps({
                    'success': False,
                    'message': 'SMS service module not properly loaded. Check server logs.'
                }),
                status_code=500,
                mimetype="application/json"
            )

        # Create SMS request
        sms_request = SmsRequest(
            sender=req_body.get('sender'),
            message=req_body.get('message'),
            phone_numbers=phone_numbers,
            validity=req_body.get('validity'),
            schedule_time=req_body.get('scheduleTime'),
            type=req_body.get('type'),
            short_link=req_body.get('shortLink'),
            webhook_url=req_body.get('webhookUrl'),
            external_id=req_body.get('externalId')
        )

        logging.info(f"Created SMS request: sender={sms_request.sender}, message={sms_request.message}, phone_numbers={sms_request.phone_numbers}")

        # Send SMS
        response = sms_service.send_sms(sms_request)

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
        logging.error(f"Error processing SMS request: {e}")
        return func.HttpResponse(
            json.dumps({
                'success': False,
                'message': f'An error occurred: {str(e)}'
            }),
            status_code=500,
            mimetype="application/json"
        )