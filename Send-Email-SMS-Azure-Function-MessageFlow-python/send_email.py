import logging
import azure.functions as func
import json
import os
from email_service import EmailService, EmailRequest, EmailRecipient, EmailContent, EmailAttachment

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

def main(req: func.HttpRequest) -> func.HttpResponse:
    logging.info('Processing Email send request.')
    try:
        req_body = req.get_json()
    except ValueError:
        return func.HttpResponse("Invalid JSON body.", status_code=400)

    # required fields
    subject = req_body.get('subject')
    smtp_account = req_body.get('smtp_account')
    from_email = req_body.get('from_email')
    to_emails = req_body.get('to_emails')
    html_content = req_body.get('html_content')
    text_content = req_body.get('text_content')

    if not subject or not smtp_account or not from_email or not to_emails:
        return func.HttpResponse("Missing required fields: subject, smtp_account, from_email, to_emails.", status_code=400)

    if isinstance(to_emails, str):
        to_emails = [e.strip() for e in to_emails.split(',') if e.strip()]
    to_recipients = [EmailRecipient(email=e) for e in to_emails]

    # optional fields
    from_name = req_body.get('from_name')
    to_names = req_body.get('to_names')
    tags = req_body.get('tags')
    cc_emails = req_body.get('cc_emails')
    cc_names = req_body.get('cc_names')
    bcc_emails = req_body.get('bcc_emails')
    bcc_names = req_body.get('bcc_names')
    reply_to_email = req_body.get('reply_to_email')
    reply_to_name = req_body.get('reply_to_name')
    headers = req_body.get('headers')
    global_vars = req_body.get('global_vars')
    attachments = req_body.get('attachments')
    template_id = req_body.get('template_id')

    # assign names to recipients if provided
    if to_names and isinstance(to_names, list):
        for i, name in enumerate(to_names):
            if i < len(to_recipients):
                to_recipients[i].name = name

    cc_recipients = None
    if cc_emails:
        if isinstance(cc_emails, str):
            cc_emails = [e.strip() for e in cc_emails.split(',') if e.strip()]
        cc_recipients = [EmailRecipient(email=e) for e in cc_emails]
        if cc_names and isinstance(cc_names, list):
            for i, name in enumerate(cc_names):
                if i < len(cc_recipients):
                    cc_recipients[i].name = name

    bcc_recipients = None
    if bcc_emails:
        if isinstance(bcc_emails, str):
            bcc_emails = [e.strip() for e in bcc_emails.split(',') if e.strip()]
        bcc_recipients = [EmailRecipient(email=e) for e in bcc_emails]
        if bcc_names and isinstance(bcc_names, list):
            for i, name in enumerate(bcc_names):
                if i < len(bcc_recipients):
                    bcc_recipients[i].name = name

    reply_to = None
    if reply_to_email:
        reply_to = EmailRecipient(email=reply_to_email, name=reply_to_name)

    email_content = EmailContent(
        html=html_content,
        text=text_content,
        template_id=template_id
    )

    email_request = EmailRequest(
        subject=subject,
        smtp_account=smtp_account,
        from_recipient=EmailRecipient(email=from_email, name=from_name),
        to_recipients=to_recipients,
        content=email_content,
        tags=tags,
        cc_recipients=cc_recipients,
        bcc_recipients=bcc_recipients,
        reply_to=reply_to,
        headers=headers,
        global_vars=global_vars,
        attachments=[EmailAttachment(**a) for a in attachments] if attachments else None
    )

    config = get_config(req)
    if not config['rest_api']['authorization'] or not config['rest_api']['application_key']:
        return func.HttpResponse("Missing Authorization or Application-Key in headers, ENV or config.json.", status_code=401)

    email_service = EmailService(config)
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