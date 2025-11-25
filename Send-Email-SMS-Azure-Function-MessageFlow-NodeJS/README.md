# Deploy a Node.js Azure Function + MessageFlow API

Complete integration of email and SMS delivery using Azure Node.js function app and the MessageFlow API.

---

## 🔗 Azure Marketplace

If you are deploying MessageFlow through Azure Marketplace, you can find our Mix & Match - Email & SMS Communication offer here:

👉 **MessageFlow – Mix & Match - Email & SMS Communication** ([See more here](https://marketplace.microsoft.com/en-us/product/saas/vercom.mix-and-match?tab=Overview))

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free) with an active subscription
- [Visual Studio Code](https://code.visualstudio.com/download)
- [Azure Functions extension](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurefunctions) for Visual Studio Code
- [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local) version 4.x
- [Node.js 18.x or above](https://nodejs.org/) (verify with `node --version`)
- [Postman](https://www.postman.com/downloads/) (for testing)

---

## Introduction

If you want to use the SMS messaging function, select the MessageFlowSmsFunction directory, and if you want to use the email messaging function, select the MessageFlowEmailFunction directory. The example below is based on SMS, but the function is launched in the same way in both cases.

---

## Quick Start

### 1. Create a MessageFlow Account

- Register: [https://app.messageflow.com/register](https://app.messageflow.com/register)
- Confirm email and phone number
- Fill in company details and select services (SMS, Email)

### 2. Generate an API Key

- Navigate to the "Account" -> "Settings" -> "API" tab
- Click "New API Key"
- Name the key and set permissions, you need to select options "Transactional e-mail" and "Transactional SMS"
- Save the following values:
  - `ApplicationKey`
  - `Authorization`

> Keep your credentials secure — they will be needed in code!

> If you received "Creating API keys impossible until account approved by customer service office" error it might be that your account has not yet been activated. If this process takes longer than 3 hours, please contact our support team to verify your identity.

---

### 3. Additional Requirements

To send emails we require two more things:

- Domain in the FROM field must be verified. You can do this in "Email" -> "Common settings" -> "Senders authorization". More information you can read [here](https://docs.messageflow.com/technical-support-center/senders-authorization/how-to-authorize-senders-in-messageflow/domains-authorization)
- Your personal "smtpAccount", you can get it from the MessageFlow application. Navigate to "Email" -> "Email API" -> "Settings" -> "SMTP Accounts"

To send SMS messages, you need to add a sender ID. You can do this in "SMS" -> "Common settings" -> "Sender IDs". More information you can read [here](https://docs.messageflow.com/communication-channels-in-the-panel/sms/sms-common-settings/sender-ids)

---

## Repository Structure

Repository: [github.com/MessageFlow-Vercom/azure-dev](https://github.com/MessageFlow-Vercom/azure-dev)

| Folder         | Description                                                         |
| -------------- | ------------------------------------------------------------------- |
| `AppService.*` | Web app for sending emails and SMS using the API                    |
| `Function.*`   | Azure Functions handling HTTP requests and using AppService classes |

---

## Project Structure

This repository contains two separate Azure Functions, each in its own directory:

```
MessageFlowEmailFunction/          # Email function (deploy separately)
├── src/
│   ├── functions/
│   │   └── emailFunction.js       # Email HTTP trigger
│   └── services/
│       └── emailService.js        # Email service logic
├── host.json                      # Function app configuration
├── package.json                   # Node.js dependencies
├── local.settings.example.json    # Example configuration
└── .gitignore                     # Git ignore rules

MessageFlowSmsFunction/            # SMS function (deploy separately)
├── src/
│   ├── functions/
│   │   └── smsFunction.js         # SMS HTTP trigger
│   └── services/
│       └── smsService.js          # SMS service logic
├── host.json                      # Function app configuration
├── package.json                   # Node.js dependencies
├── local.settings.example.json    # Example configuration
└── .gitignore                     # Git ignore rules
```

---

## Secure Credential Storage

### Locally (`local.settings.json`):

Create a `local.settings.json` file (copy from `local.settings.example.json`):

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "node",
    "RestApiApplicationKey": "your-application-key-here",
    "RestApiAuthorization": "your-authorization-key-here"
  }
}
```

> You can copy the `local.settings.example.json` file, rename it to `local.settings.json`, and fill it in with your MessageFlow credentials.

### In Azure Portal:

- Go to: App Service -> Settings -> Environment variables
- Add two variables:
  - `RestApiApplicationKey`
  - `RestApiAuthorization`

---

## Installation

1. Clone the repository and navigate to the directory of the function you want to use:

**For Email function:**

```bash
cd Send-Email-SMS-Azure-Function-MessageFlow-NodeJS/MessageFlowEmailFunction
```

**For SMS function:**

```bash
cd Send-Email-SMS-Azure-Function-MessageFlow-NodeJS/MessageFlowSmsFunction
```

2. Install dependencies:

```bash
npm install
```

3. Configure `local.settings.json` with valid API keys (see above):

```bash
# Copy the example file
cp local.settings.example.json local.settings.json
# Then edit local.settings.json with your MessageFlow credentials
```

---

## Running the Function Locally

1. Make sure you have Node.js 18.x or above installed:

```bash
node --version
```

2. Start Azurite (local Azure Storage emulator):

   - In VS Code, press F1 and run: **Azurite: Start**
   - Or install and run manually: [Azurite Documentation](https://learn.microsoft.com/en-us/azure/storage/common/storage-use-azurite)

3. Start the function:

```bash
func start
```

Or use npm:

```bash
npm start
```

You should see output similar to:

```
Functions:
  MessageFlowEmailHttpTrigger: [POST] http://localhost:7071/api/MessageFlowEmailHttpTrigger
  MessageFlowSmsHttpTrigger: [POST] http://localhost:7071/api/MessageFlowSmsHttpTrigger
```

---

## Publish and Test Your Function

Follow these steps to publish your project:

### Create Function App in Azure

1. Clone the repository and navigate to the selected directory
2. Open Visual Studio Code from your project's root directory. If prompted, select **Yes, I trust the authors**
3. In Visual Studio Code, select **View -> Command Palette** (or press F1)
4. Search for and select: **Azure Functions: Create Function App in Azure**

Respond to the prompts as follows:

- If prompted, sign in to your Azure account
- Select the Azure subscription to use
- Enter a globally unique name that's valid in a URL path
- Select a hosting plan (e.g. **Flex Consumption**)
- Select an Azure region (for better performance, select a region near you)
- Select **Node.js 22** (or higher) as your runtime stack
- Select instance memory size (e.g. **2048**)
- Select maximum instances count(the maximum number of concurrently running instances of a function in Azure Functions)
- Create new or select a resource group
- Select resource authentication type: **Managed identity** (most secure option)
- Create new or select a user assigned identity
- Create new or select a storage account
  > Depending on your choices, some of the above questions may not appear or may appear in a different order.

The Azure extension shows the status of individual resources as they're created in Azure in the **Azure: Activity Log** panel.

### Deploy the Project to Azure

1. In Visual Studio Code, select **View -> Command Palette** (F1)
2. Search for and select: **Azure Functions: Deploy to Function App**
3. Select the function app you just created
4. When prompted about overwriting previous deployments, select **Deploy**
5. When deployment is completed, select **View Output** to see the results

### Configure Environment Variables in Azure

1. Go to Azure Portal -> Your Function App
2. Navigate to **Settings -> Environment variables**
3. Add two new application variables:
   - `RestApiApplicationKey` = your MessageFlow application key
   - `RestApiAuthorization` = your MessageFlow authorization key
4. Click **Save**

### Run the Function in Azure

1. In Visual Studio Code, select **View -> Command Palette** (F1)
2. Search for and select: **Azure Functions: Execute Function Now...**
3. Select your deployed function (MessageFlowEmailHttpTrigger or MessageFlowSmsHttpTrigger)
4. Copy and paste a sample payload (see below)

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

---

## Testing with Postman

### Request Setup:

- Method: `POST`
- URL (local):
  - `http://localhost:7071/api/MessageFlowEmailHttpTrigger`
  - `http://localhost:7071/api/MessageFlowSmsHttpTrigger`
- URL (Azure):
  - `https://your-function-app.azurewebsites.net/api/MessageFlowEmailHttpTrigger`
  - `https://your-function-app.azurewebsites.net/api/MessageFlowSmsHttpTrigger`
- Headers:
  - `Content-Type: application/json`
- Body:
  - Choose `raw -> JSON`
  - Paste a sample payload

---

## Data Flow Diagram

```
[Client Request]
      ↓
[Azure Function (Node.js)]
      ↓
[EmailService / SmsService]
      ↓
[MessageFlow API]
      ↓
[Client Response]
```

---

## Key Features - Node.js v4 Programming Model

This implementation uses the **Azure Functions Node.js v4 programming model**, which provides:

### Modern Function Registration

Functions are registered using the `app` object from `@azure/functions`:

```javascript
const { app } = require("@azure/functions");

app.http("MessageFlowEmailHttpTrigger", {
  methods: ["POST"],
  authLevel: "anonymous",
  handler: async (request, context) => {
    // Function logic here
  },
});
```

### Context Object

The `context` object provides structured logging and metadata:

```javascript
context.log("Info message"); // Information logging
context.warn("Warning message"); // Warning logging
context.error("Error message"); // Error logging
```

### Request/Response Handling

Uses the Fetch API standard for HTTP handling:

```javascript
// Read request body
const body = await request.text();
const data = JSON.parse(body);

// Return response
return {
  status: 200,
  body: JSON.stringify({ success: true }),
  headers: { "Content-Type": "application/json" },
};
```

### Service Layer Architecture

- **emailService.js** - Handles all email-related API calls to MessageFlow
- **smsService.js** - Handles all SMS-related API calls to MessageFlow
- Services are instantiated once and reused across invocations for better performance

---

## Your Own Solution

You can also copy the `emailService.js` or `smsService.js` file and use them in your own Node.js code. Both services are self-contained and only require:

- MessageFlow API credentials (via environment variables)
- Node.js built-in `https` module

Example usage:

```javascript
const EmailService = require("./services/emailService");
const emailService = new EmailService();

const result = await emailService.sendEmailAsync(emailRequest, context);
```

---

## Troubleshooting

### Function not starting locally

- Make sure Azurite is running (F1 -> Azurite: Start)
- Check that Node.js version is 18.x or above
- Verify `local.settings.json` exists and contains valid configuration

### API returns 401 Unauthorized

- Check that `RestApiApplicationKey` and `RestApiAuthorization` are set correctly
- Verify the API keys in MessageFlow dashboard

---

## Contributors

- Created by: [MessageFlow](https://dev.messageflow.com)

## About MessageFlow

MessageFlow is a developer-friendly communication platform designed to help teams deliver secure, high-quality transactional messaging at scale.

With our Email & SMS API you can:

- Send fast, reliable transactional emails from any system
- Track delivery, errors and engagement in real time
- Improve deliverability with verified domains and optimized infrastructure
- Simplify integration with transparent pricing and predictable performance.