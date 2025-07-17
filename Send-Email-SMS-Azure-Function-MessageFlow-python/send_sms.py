import logging
import azure.functions as func
import json
import os
from sms_service import SmsService, SmsRequest

def get_config(req: func.HttpRequest):
    authorization = req.headers.get('Authorization')
    application_key = req.headers.get('Application-Key')

    if not authorization:
        authorization = os.environ.get('AUTHORIZATION')
    if not application_key:
        application_key = os.environ.get('APPLICATION_KEY')

    if not authorization or not application_key:
        try:
            with open('config.json', 'r') as f:
                config_json = json.load(f)
                if not authorization:
                    authorization = config_json.get('rest_api', {}).get('authorization')
                if not application_key:
                    application_key = config_json.get('rest_api', {}).get('application_key')
        except Exception:
            pass

    return {
        'rest_api': {
            'authorization': authorization,
            'application_key': application_key
        }
    }

def sendSmsHttpTrigger(req: func.HttpRequest) -> func.HttpResponse:
    logging.info('Processing SMS send request.')

    try:
        req_body = req.get_json()
    except ValueError:
        return func.HttpResponse("Invalid JSON body.", status_code=400)

    sender = req_body.get('sender')
    message = req_body.get('message')
    phonenumbers = req_body.get('phonenumbers')
    validity = req_body.get('validity')
    schedule_time = req_body.get('schedule_time')
    type_ = req_body.get('type')
    short_link = req_body.get('short_link')
    webhook_url = req_body.get('webhook_url')
    external_id = req_body.get('external_id')

    # required fields
    if not sender or not message or not phonenumbers:
        return func.HttpResponse(
            "Missing required fields: sender, message, phonenumbers.",
            status_code=400
        )

    # phonenumbers parsing
    if isinstance(phonenumbers, str):
        phone_numbers = [p.strip() for p in phonenumbers.split(',') if p.strip()]
    elif isinstance(phonenumbers, list):
        phone_numbers = phonenumbers
    else:
        return func.HttpResponse("phonenumbers must be a list or comma-separated string.", status_code=400)

    config = get_config(req)
    if not config['rest_api']['authorization'] or not config['rest_api']['application_key']:
        return func.HttpResponse("Missing Authorization or Application-Key in headers, ENV or config.json.", status_code=401)

    sms_service = SmsService(config)
    sms_request = SmsRequest(
        sender=sender,
        message=message,
        phone_numbers=phone_numbers,
        validity=validity,
        schedule_time=schedule_time,
        type=type_,
        short_link=short_link,
        webhook_url=webhook_url,
        external_id=external_id
    )
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