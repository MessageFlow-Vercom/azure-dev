import json
import requests
import logging
from typing import List, Optional, Dict, Any
from dataclasses import dataclass, asdict

@dataclass
class SmsRequest:
    sender: str
    message: str
    phone_numbers: List[str]
    validity: Optional[int] = None
    schedule_time: Optional[int] = None
    type: Optional[int] = None
    short_link: Optional[bool] = None
    webhook_url: Optional[str] = None
    external_id: Optional[str] = None

@dataclass
class SmsResponse:
    success: bool
    message: str
    response_content: str = ""

class SmsService:
    def __init__(self, config: Dict[str, Any]):
        """
        Initialize SMS service with configuration
        
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
    
    def send_sms(self, request: SmsRequest) -> SmsResponse:
        """
        Send SMS using MessageFlow API
        
        Args:
            request: SmsRequest object containing SMS details
            
        Returns:
            SmsResponse object with success status and message
        """
        self.logger.info(f"SmsService.send_sms called with request: {json.dumps(asdict(request), indent=2)}")
        
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
                'sender': request.sender,
                'message': request.message,
                'phoneNumbers': request.phone_numbers,
                'validity': request.validity,
                'scheduleTime': request.schedule_time,
                'type': request.type,
                'shortLink': request.short_link,
                'webhookUrl': request.webhook_url,
                'externalId': request.external_id
            }
            
            # Remove None values
            payload = {k: v for k, v in payload.items() if v is not None}
            
            json_payload = json.dumps(payload, indent=2)
            self.logger.info(f"SMS API Request Payload: {json_payload}")
            
            # Send request
            self.logger.info("Sending HTTP request to: https://api.messageflow.com/v2.1/sms")
            
            response = requests.post(
                'https://api.messageflow.com/v2.1/sms',
                headers=headers,
                json=payload,
                timeout=30
            )
            
            response_content = response.text
            self.logger.info(f"SMS API Response Status: {response.status_code}")
            self.logger.info(f"SMS API Response Content: {response_content}")
            
            # Parse response
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    if isinstance(response_data, dict):
                        return SmsResponse(
                            success=True,
                            message=response_data.get('message', 'SMS sent successfully'),
                            response_content=response_content
                        )
                except (json.JSONDecodeError, KeyError) as e:
                    self.logger.warning(f"Can not parse response to SmsResponse model, but HTTP status is success: {e}")
                
                return SmsResponse(
                    success=True,
                    message="SMS sent successfully",
                    response_content=response_content
                )
            else:
                return SmsResponse(
                    success=False,
                    message=f"HTTP Error: {response.status_code}",
                    response_content=response_content
                )
                
        except requests.exceptions.RequestException as ex:
            self.logger.error(f"HTTP request failed: {ex}")
            return SmsResponse(
                success=False,
                message=f"HTTP request failed: {str(ex)}",
                response_content=str(ex)
            )
        except Exception as ex:
            self.logger.error(f"Error sending SMS: {ex}")
            return SmsResponse(
                success=False,
                message=f"Error: {str(ex)}",
                response_content=str(ex)
            ) 