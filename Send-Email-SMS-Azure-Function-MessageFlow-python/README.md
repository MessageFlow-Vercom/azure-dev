# Deploy an Python function app + MessageFlow API

Complete integration of email and SMS delivery using Azure Python function app and the MessageFlow API.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free/dotnet) with an active subscription.
- [Visual Studio Code](https://www.visualstudio.com/downloads)
- The [Python extension](https://marketplace.visualstudio.com/items?itemName=ms-python.python) for Visual Studio Code.
- The [Azure Functions extension](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurefunctions) for Visual Studio Code, version 1.8.1 or later.
- [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local)
- [Python 3.12 or higher](https://www.python.org/downloads/) installed locally.

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

### Locally (`local.settings.json`):

```json
{
  "Values": {
    "RestApi__ApplicationKey": "your-application-key",
    "RestApi__Authorization": "your-authorization-key"
  }
}
```

You can copy the local.settings.example.json file, rename it to local.settings.json, and fill it in with your MessageFlow credentials.

### Deployment to Azure (`config.json`):

```json
{
  "rest_api": {
    "application_key": "your-messageflow-application-key-here",
    "authorization": "your-messageflow-authorization-token-here"
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

## Running the function locally

To run the application locally:

1. Install Azure Functions Core Tools
2. Go to function dir
3. Install requirements:
   ```bash
   pip install -r requirements.txt
   ```
4. Run function:
   ```bash
   func start
   ```

<img width="732" height="197" alt="pythonfunc1" src="https://github.com/user-attachments/assets/a9c367dc-7e5b-40fc-afca-55491eeee7b0" />

---

## Publish and test your function

Follow these steps to publish your project:

- Clone the repository and navigate to the selected function directory
- Open Visual Studio Code from your project's root directory. If prompted, select Yes, I trust the authors.
- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions: Create Function App in Azure.

<img width="1222" height="204" alt="pythonfunc2" src="https://github.com/user-attachments/assets/d756ce2e-2367-424c-b7a2-7267bbf777c4" />

Respond to the prompts as follows:

- If prompted, sign in to your Azure account.
- Select the Azure subscription to use. The prompt doesn't appear when you have only one subscription visible under Resources.
- Enter a globally unique name that's valid in a URL path. The name you enter is validated to make sure that it's unique in Azure Functions.
- Select an Azure region. For better performance, select a region near you.
- Select the runtime stack for your function app (select the language version you currently run locally, e.g., .NET 8 Isolated).
- Select resource authentication type. Select Managed identity, which is the most secure option for connecting to the default host storage account.

In the Azure: Activity Log panel, the Azure extension shows the status of individual resources as they're created in Azure.

<img width="1234" height="224" alt="pythonfunc3" src="https://github.com/user-attachments/assets/0170a166-3a59-4789-b798-04439a376c8c" />

# Deploy project to Azure:

- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions: Deploy to Function App.
- Select the function app you just created. When prompted about overwriting previous deployments, select Deploy to deploy your function code to the new function app resource.
- When deployment is completed, select View Output to view the creation and deployment results, including the Azure resources that you created. If you miss the notification, select the bell icon in the lower-right corner to see it again.

<img width="1234" height="235" alt="pythonfunc4" src="https://github.com/user-attachments/assets/eab87653-ad64-49f7-b28e-ccc8499a82b4" />

Run the function in Azure

- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions:Execute Function Now...
- Select recently deployed function
- Copy and paste payload

---

## Sample JSON Payloads

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
  - email: `https://your-function-url/api/MessageFlowEmailHttpTrigger`
  - sms: `https://your-function-url/api/MessageFlowSmsHttpTrigger`
- Headers:
  - `Content-Type: application/json`
- Body:
  - Choose `raw → JSON`
  - Paste a sample payload

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
