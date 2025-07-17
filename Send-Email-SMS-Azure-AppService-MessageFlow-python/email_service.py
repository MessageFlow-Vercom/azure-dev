import json
import requests
import logging
from typing import List, Optional, Dict, Any
from dataclasses import dataclass, asdict

@dataclass
class EmailRecipient:
    email: str
    name: Optional[str] = None
    message_id: Optional[str] = None
    vars: Optional[Dict[str, Any]] = None

@dataclass
class EmailContent:
    html: Optional[str] = None
    text: Optional[str] = None
    template_id: Optional[str] = None

@dataclass
class EmailAttachment:
    file_name: str
    file_mime: str
    file_content: str
    inline: bool = False

@dataclass
class EmailRequest:
    subject: str
    smtp_account: str
    from_recipient: EmailRecipient
    to_recipients: List[EmailRecipient]
    content: EmailContent
    tags: Optional[List[str]] = None
    cc_recipients: Optional[List[EmailRecipient]] = None
    bcc_recipients: Optional[List[EmailRecipient]] = None
    reply_to: Optional[EmailRecipient] = None
    headers: Optional[Dict[str, str]] = None
    global_vars: Optional[Dict[str, Any]] = None
    attachments: Optional[List[EmailAttachment]] = None

@dataclass
class EmailResponse:
    success: bool
    message: str
    response_content: str = ""

class EmailService:
    def __init__(self, config: Dict[str, Any]):
        """
        Initialize Email service with configuration
        
        Args:
            config: Dictionary containing 'rest_api' configuration with 'authorization' and 'application_key'
        """
        self.config = config
        self.logger = logging.getLogger(__name__)
        
        # Validate configuration
        if not config.get('rest_api', {}).get('authorization'):
            raise ValueError("REST API authorization is missing in configuration")
        if not config.get('rest_api', {}).get('application_key'):
            raise ValueError("REST API application key is missing in configuration")
    
    def send_email(self, request: EmailRequest) -> EmailResponse:
        """
        Send email using MessageFlow API
        
        Args:
            request: EmailRequest object containing email details
            
        Returns:
            EmailResponse object with success status and message
        """
        self.logger.info(f"EmailService.send_email called with request: {json.dumps(asdict(request), indent=2)}")
        
        try:
            # Prepare headers
            headers = {
                'Authorization': self.config['rest_api']['authorization'],
                'Application-Key': self.config['rest_api']['application_key'],
                'Content-Type': 'application/json'
            }
            
            self.logger.info(f"Headers set - Authorization: {self.config['rest_api']['authorization'][:10]}..., Application-Key: {self.config['rest_api']['application_key'][:10]}...")
            
            # Prepare payload
            payload = {
                'subject': request.subject,
                'smtpAccount': request.smtp_account,
                'tags': request.tags,
                'content': asdict(request.content),
                'bcc': [asdict(recipient) for recipient in request.bcc_recipients] if request.bcc_recipients else None,
                'cc': [asdict(recipient) for recipient in request.cc_recipients] if request.cc_recipients else None,
                'from': asdict(request.from_recipient),
                'replyTo': asdict(request.reply_to) if request.reply_to else None,
                'headers': request.headers,
                'globalVars': request.global_vars,
                'to': [asdict(recipient) for recipient in request.to_recipients],
                'attachments': [asdict(attachment) for attachment in request.attachments] if request.attachments else None
            }
            
            # Remove None values
            payload = {k: v for k, v in payload.items() if v is not None}
            
            json_payload = json.dumps(payload, indent=2)
            self.logger.info(f"Email API Request Payload: {json_payload}")
            
            # Send request
            self.logger.info("Sending HTTP request to: https://api.messageflow.com/v2.1/email")
            
            response = requests.post(
                'https://api.messageflow.com/v2.1/email',
                headers=headers,
                json=payload,
                timeout=30
            )
            
            response_content = response.text
            self.logger.info(f"Email API Response Status: {response.status_code}")
            self.logger.info(f"Email API Response Content: {response_content}")
            
            # Parse response
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    if isinstance(response_data, dict):
                        return EmailResponse(
                            success=True,
                            message=response_data.get('message', 'SMS sent successfully'),
                            response_content=response_content
                        )
                except (json.JSONDecodeError, KeyError) as e:
                    self.logger.warning(f"Cant parse response to EmailResponse model, but HTTP status is success: {e}")
                
                return EmailResponse(
                    success=True,
                    message="Email sent successfully",
                    response_content=response_content
                )
            else:
                return EmailResponse(
                    success=False,
                    message=f"HTTP Error: {response.status_code}",
                    response_content=response_content
                )
                
        except requests.exceptions.RequestException as ex:
            self.logger.error(f"HTTP request failed: {ex}")
            return EmailResponse(
                success=False,
                message=f"HTTP request failed: {str(ex)}",
                response_content=str(ex)
            )
        except Exception as ex:
            self.logger.error(f"Error sending email: {ex}")
            return EmailResponse(
                success=False,
                message=f"Error: {str(ex)}",
                response_content=str(ex)
            ) 