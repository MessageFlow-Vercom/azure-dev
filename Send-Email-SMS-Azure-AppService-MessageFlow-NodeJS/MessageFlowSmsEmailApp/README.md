# Deploy a Node.js web app + MessageFlow API

Complete integration of email and SMS delivery using Azure Node.js web app and the MessageFlow API.

---

## 🔗 Azure Marketplace

If you are deploying MessageFlow through Azure Marketplace, you can find our Mix & Match - Email & SMS Communication offer here:

👉 **MessageFlow – Mix & Match - Email & SMS Communication** ([See more here](https://marketplace.microsoft.com/en-us/product/saas/vercom.mix-and-match?tab=Overview))

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free/dotnet) with an active subscription.
- [Visual Studio Code](https://www.visualstudio.com/downloads)
- [Azure Tools extension pack](https://marketplace.visualstudio.com/items?itemName=ms-vscode.vscode-node-azure-pack) installed and be signed into Azure from VS Code.
- [Node.js 18 or higher](https://nodejs.org/) installed locally.

---

## Quick Start

### 1. Create a MessageFlow Account

- Register: [https://app.messageflow.com/register](https://app.messageflow.com/register)
- Confirm email and phone number
- Fill in company details and select services (SMS, Email)

### 2. Generate an API Key

- Navigate to the "Account"->"Settings"->"API" tab
- Click "New API Key"
- Name the key and set permissions, you need to select options "Transactional e-mail" and "Transactional SMS"
- Save the following values:
  - `ApplicationKey`
  - `Authorization`

> Keep your credentials secure — they will be needed in code!

> If you received "Creating API keys impossible until account approved by customer service office" error it might be that your account has not yet been activated. If this process takes longer than 3 hours, please contact our support team to verify your identity.

---

### 3. Additional requirements

To send emails we require two more things:

- domain in the FROM field must be verified. You can do this in "Email"->"Common settings"->"Senders authorization". More information you can read [here](https://docs.messageflow.com/technical-support-center/senders-authorization/how-to-authorize-senders-in-messageflow/domains-authorization)
- your personal "smtpAccount", you can get it from the MessageFlow application. Navigate to "Email"->"Email API"->"Settings"->"SMTP Accounts"

To send SMS messages, you need to add a sender ID. You can do this in "SMS"->"Common settings"->"Sender IDs". More information you can read [here](https://docs.messageflow.com/communication-channels-in-the-panel/sms/sms-common-settings/sender-ids)

---

## Repository Structure

Repository: [github.com/MessageFlow-Vercom/azure-dev](https://github.com/MessageFlow-Vercom/azure-dev)

| Folder         | Description                                                         |
| -------------- | ------------------------------------------------------------------- |
| `AppService.*` | Web app for sending emails and SMS using the API                    |
| `Function.*`   | Azure Functions handling HTTP requests and using AppService classes |

---

## Secure Credential Storage

### Locally (`config.json`)

```json
{
  "rest_api": {
    "application_key": "your-application-key",
    "authorization": "your-authorization-key"
  }
}
```

You can copy the config.example.json file, rename it to config.json, and fill it in with your MessageFlow credentials.

### In Azure Portal (Recommended for Production)

Azure App Service automatically provides the `PORT` environment variable. You need to add your MessageFlow credentials:

- Go to: App Service → Configuration → Application Settings
- Add two variables:
  - `REST_API_AUTHORIZATION` = your-authorization-key
  - `REST_API_APPLICATION_KEY` = your-application-key

**Important**: After adding configuration, restart your App Service for changes to take effect.

---

## Running the web app locally

To run the application locally (with Express):

1. Go to the application folder:

```bash
cd MessageFlowSmsEmailApp
```

2. Install Node.js dependencies:

```bash
npm install
```

3. Configure credentials (choose one method):

**Option A: Using config.json (Recommended for local development)**

```bash
cp config.example.json config.json
```

Edit `config.json` with your MessageFlow credentials.

**Option B: Using Environment Variables**

```bash
export REST_API_AUTHORIZATION="your-authorization-key"
export REST_API_APPLICATION_KEY="your-application-key"
```

4. Run the app:

```bash
npm start
```

For development with auto-reload:

```bash
npm run dev
```

5. Browse to the application at http://localhost:3000 in a web browser.

**Note**: The app listens on the port specified by the `PORT` environment variable, or defaults to 3000 if not set.

---

## Publish and test your web app

Follow these steps to create your App Service resources and publish your project:

- Clone the repository and navigate to the selected directory
- Open Visual Studio Code from your project's root directory. If prompted, select Yes, I trust the authors.
- Locate the Azure icon in the left-hand toolbar. Select it to bring up the Azure Tools for VS Code extension.

<img width="668" height="287" alt="nodejsapp1" src="https://github.com/user-attachments/assets/10243f70-ff9c-4a3f-b6c6-2177991fe96f" />

> If you do not see the Azure Tools icon, make sure you have the Azure Tools extension for VS Code installed.

In the Azure Tools extension for VS Code:

- Find the RESOURCES section and select your subscription.
- Select + (Create Resource...)
- Choose the "Create App Service Web App..." option.

  <img width="948" height="131" alt="nodejs2" src="https://github.com/user-attachments/assets/2d0b73b5-f8ee-4069-b44c-48e45841b3f7" />

- Select the region where you want to host your web app.
- Enter the name messageflow-nodejs-webapp-XYZ for this web app, where XYZ is any three unique characters. When deployed, this name is used as your app name.
- Select the runtime stack for the application. In this project, select Node 18 LTS or higher.
- Select the App Service plan (pricing tier) for this web app. The App Service plan controls how many resources (CPU/memory) are available to your app and how much you pay.

<img width="1230" height="148" alt="nodejsapp3" src="https://github.com/user-attachments/assets/24f021f6-bdf0-4e5f-ac32-9414e08be1a8" />

- Go to the RESOURCES section and select your subscription.
- Inside App Services right click on messageflow-nodejs-webapp-XYZ web app and select "Deploy to web app..."

<img width="510" height="250" alt="nodejsapp4" src="https://github.com/user-attachments/assets/a9924382-b475-45a4-8913-8942886911e8" />

- Select the MessageFlowSmsEmailApp folder you are working in as the one to deploy.
- Select resource (messageflow-nodejs-webapp-XYZ for this web app)
- When prompted to update build configuration, select **Yes** to enable build automation during deployment
- When the deployment is complete, a notification will appear in the lower right corner of VS Code. You can use this notification to browse to your web app (click "Browse Website" button)

### Configure Application Settings in Azure

After deployment, you must configure your MessageFlow credentials:

1. In Azure Portal, navigate to your App Service
2. Go to **Configuration** → **Application settings**
3. Click **+ New application setting** and add:
   - Name: `REST_API_AUTHORIZATION`, Value: your-authorization-key
   - Name: `REST_API_APPLICATION_KEY`, Value: your-application-key
4. Click **Save** and then **Continue** to restart the app

<img width="1232" height="400" alt="nodejsapp5" src="https://github.com/user-attachments/assets/f35dd2de-4b64-4569-8dd1-3fc55ac5f26e" />

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

### Request Setup

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
[Express App - Email / SMS]
      ↓
[MessageFlow API]
      ↓
[Client Response]
```

---

## Your own solution

You can also copy the SmsService or EmailService files and use them in your code.

---

## Project Structure

```
MessageFlowSmsEmailApp/
├── app.js                    # Main Express application
├── package.json              # Node.js dependencies and scripts
├── config.json               # Configuration file (not in git)
├── config.example.json       # Example configuration
├── .gitignore                # Git ignore rules
└── services/
    ├── smsService.js         # SMS service implementation
    └── emailService.js       # Email service implementation
```

---

## Available Scripts

- `npm start` - Start the application
- `npm run dev` - Start with nodemon for development (auto-reload)

---

## Dependencies

### Production Dependencies:
- **express**: Fast, unopinionated web framework for Node.js
- **axios**: Promise-based HTTP client for API requests

### Development Dependencies:
- **nodemon**: Auto-reload utility for development

---

## Contributors

- Created by: MessageFlow

## About MessageFlow

MessageFlow is a developer-friendly communication platform designed to help teams deliver secure, high-quality transactional messaging at scale.

With our Email & SMS API you can:

- Send fast, reliable transactional emails from any system
- Track delivery, errors and engagement in real time
- Improve deliverability with verified domains and optimized infrastructure
- Simplify integration with transparent pricing and predictable performance.

---