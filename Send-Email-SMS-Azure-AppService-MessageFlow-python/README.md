# Deploy an Python web app + MessageFlow API

Complete integration of email and SMS delivery using Azure Python web app and the MessageFlow API.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free/dotnet) with an active subscription.
- [Visual Studio Code](https://www.visualstudio.com/downloads)
- [Azure Tools extension pack](https://marketplace.visualstudio.com/items?itemName=ms-vscode.vscode-node-azure-pack) installed and be signed into Azure from VS Code.
- [Python 3.12 or higher](https://www.python.org/downloads/) installed locally.

---

## Quick Start

### 1. Create a MessageFlow Account

- Register: [https://app.messageflow.com/register](https://app.messageflow.com/register)
- Confirm email and phone number
- Fill in company details and select services (SMS, Email)

### 2. Generate an API Key

- Navigate to the “Account”->“Settings”->“API” tab
- Click “New API Key”
- Name the key and set permissions, you need to select options “Transactional e-mail” and “Transactional SMS”
- Save the following values:
  - `ApplicationKey`
  - `Authorization`

> Keep your credentials secure — they will be needed in code!

> If you received "Creating API keys impossible until account approved by customer service office" error it might be that your account has not yet been activated. If this process takes longer than 3 hours, please contact our support team to verify your identity.

---

### 3. Additional requirements

To send emails we require two more things:

- domain in the FROM field must be verified. You can do this in “Email”->“Common settings”->“Senders authorization”. More information you can read [here](https://docs.messageflow.com/technical-support-center/senders-authorization/how-to-authorize-senders-in-messageflow/domains-authorization)
- your personal "smtpAccount", you can get it from the MessageFlow application. Navigate to “Email”->“Email API”->“Settings”->“SMTP Accounts”

To send SMS messages, you need to add a sender ID. You can do this in “SMS”->“Common settings”->“Sender IDs”. More information you can read [here](https://docs.messageflow.com/communication-channels-in-the-panel/sms/sms-common-settings/sender-ids)

---

## Repository Structure

Repository: [github.com/MessageFlow-Vercom/azure-dev](https://github.com/MessageFlow-Vercom/azure-dev)

| Folder         | Description                                                         |
| -------------- | ------------------------------------------------------------------- |
| `AppService.*` | Web app for sending emails and SMS using the API                    |
| `Function.*`   | Azure Functions handling HTTP requests and using AppService classes |

---

## Secure Credential Storage

### Locally (`config.json`):

```json
{
  "rest_api": {
    "application_key": "your-application-key",
    "authorization": "your-authorization-key"
  }
}
```

You can copy the config.example.json file, rename it to config.json, and fill it in with your MessageFlow credentials.

### In Azure Portal:

- Go to: App Service → Configuration → Application Settings
- Add two variables:
  - `ApplicationKey`
  - `Authorization`

---

## Running the web app locally

To run the application locally (with Flask):

1. Go to the application folder:
2. Create a virtual environment for the app:

On Windows:

```bash
py -m venv .venv
.venv\scripts\activate
```

On macOS/Linux:

```bash
python3 -m venv .venv
source .venv/bin/activate
```

3. Install the dependencies:

```bash
pip install -r requirements.txt
```

4. Run the app:

```bash
flask run
```

5. Browse to the application at http://localhost:5000 in a web browser.

---

## Publish and test your web app

Follow these steps to create your App Service resources and publish your project:

- Clone the repository and navigate to the selected directory
- Open Visual Studio Code from your project's root directory. If prompted, select Yes, I trust the authors.
- Locate the Azure icon in the left-hand toolbar. Select it to bring up the Azure Tools for VS Code extension.

<img width="668" height="287" alt="pythonapp1" src="https://github.com/user-attachments/assets/10243f70-ff9c-4a3f-b6c6-2177991fe96f" />

> If you do not see the Azure Tools icon, make sure you have the Azure Tools extension for VS Code installed.

In the Azure Tools extension for VS Code:

- Find the RESOURCES section and select your subscription.
- Select + (Create Resource...)
- Choose the "Create App Service Web App..." option.

  <img width="948" height="131" alt="python2" src="https://github.com/user-attachments/assets/2d0b73b5-f8ee-4069-b44c-48e45841b3f7" />

- Select the region where you want to host your web app.
- Enter the name messageflow-python-webapp-XYZ for this web app, where XYZ is any three unique characters. When deployed, this name is used as your app name.
- Select the runtime stack for the application. In this project, select Python 3.13.
- Select the App Service plan (pricing tier) for this web app. The App Service plan controls how many resources (CPU/memory) are available to your app and how much you pay.

<img width="1230" height="148" alt="pythonapp3" src="https://github.com/user-attachments/assets/24f021f6-bdf0-4e5f-ac32-9414e08be1a8" />

- Go to the RESOURCES section and select your subscription.
- Inside App Services right click on messageflow-python-webapp-XYZ web app and select "Deploy to web app..."

<img width="510" height="250" alt="pythonapp4" src="https://github.com/user-attachments/assets/a9924382-b475-45a4-8913-8942886911e8" />

- Select the quickstart folder you are working in as the one to deploy.
- Select resource (messageflow-python-webapp-XYZ for this web app)
- Answer Yes to update your build configuration and improve deployment performance.
- When the deployment is complete, a notification will appear in the lower right corner of VS Code. You can use this notification to browse to your web app (click "Browse Website" button)

<img width="1232" height="400" alt="pythonapp5" src="https://github.com/user-attachments/assets/f35dd2de-4b64-4569-8dd1-3fc55ac5f26e" />

A web page will open, and if everything worked correctly, you will see the message:

```
MessageFlow SMS/Email API app is running. Use POST /sms or POST /email endpoints.
```

> You can copy the web page URL and use it during testing with Postman

---

## Sample JSON Payloads

### Email

```json
{
  "subject": "Welcome to MessageFlow!",
  "smtpAccount": "1.example.smtp",
  "to": [
    {
      "email": "user1@example.com",
      "name": "User 1"
    },
    {
      "email": "user2@example.com",
      "name": "User 2"
    }
  ],
  "cc": [
    {
      "email": "cc@example.com",
      "name": "CC User"
    }
  ],
  "bcc": [
    {
      "email": "bcc@example.com",
      "name": "BCC User"
    }
  ],
  "content": {
    "html": "<h1>Hello!</h1><p>This is a test email from Azure.</p><p>Thanks for your attention!</p>",
    "text": "Hello! This is a test email from Azure. Thanks for your attention!"
  },
  "from": {
    "email": "from@example.com",
    "name": "Azure Team"
  },
  "replyTo": {
    "email": "reply@example.com",
    "name": "Reply Handler"
  },
  "tags": ["azure", "messageflow", "test"],
  "headers": {
    "X-Custom-Header": "CustomValue",
    "X-Priority": "High"
  },
  "globalVars": {
    "company": "Messageflow",
    "environment": "development"
  }
}
```

### SMS

```json
{
  "sender": "senderName",
  "message": "Azure test message from API",
  "phoneNumbers": ["+48123456789", "+48987654321"],
  "validity": 1,
  "type": 0,
  "externalId": "test-sms-001"
}
```

## Success Response

```json
{
  "success": true,
  "message": "SMS sent successfully! Data count: 1",
  "response_content": "{\"data\":[...]}"
}
```

## Error Response

```json
{
  "success": false,
  "message": "Missing required field: phoneNumbers (must be a non-empty array)"
}
```

---

## Testing with Postman

### Request Setup:

- Method: `POST`
- URL:
  - `https://your-app-url/email`
  - `https://your-app-url/sms`
- Headers:
  - `Content-Type: application/json`
- Body:
  - Choose `raw → JSON`
  - Paste a sample payload

### Optional: Environment Variables in Postman

- Add `Authorization` and `ApplicationKey` as environment variables
- Use them as `{{Authorization}}` and `{{ApplicationKey}}`

---

## Data Flow Diagram

```
[Client Request]
      ↓
[Azure]
      ↓
[AppService - Email / Sms]
      ↓
[MessageFlow API]
      ↓
[Client Response]
```

---

## Your own solution

You can also copy the SmsService or EmailService files and use them in your code.

---

## Contributors

- Created by: MessageFlow
