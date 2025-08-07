# Deploy an ASP.NET function app + MessageFlow API

Complete integration of email and SMS delivery using Azure ASP.NET function app and the MessageFlow API.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free/dotnet) with an active subscription.
- [Visual Studio Code](https://www.visualstudio.com/downloads)
- [C# extension](https://marketplace.visualstudio.com/items?itemName=ms-dotnettools.csharp) for Visual Studio Code.
- [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local)
- [Azure Functions extension](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurefunctions) for Visual Studio Code.
- [The latest .NET 8.0 SDK](https://dotnet.microsoft.com/download/dotnet/8.0)
- [Postman](https://www.postman.com/downloads/) (for testing)

---

## Quick Start

### 1. Create a MessageFlow Account

- Register: [https://app.messageflow.com/register](https://app.messageflow.com/register)
- Confirm email and phone number
- Fill in company details and select services (SMS, Email)

### 2. Generate an API Key

- Navigate to the “API Settings” tab
- Click “New API Key”
- Name the key and set permissions
- Save the following values:
  - `Authorization`
  - `ApplicationKey`

> Keep your credentials secure — they will be needed in code!

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
    "RestApiAuthorization": "your-auth-key",
    "RestApiApplicationKey": "your-app-key"
  }
}
```

> You can copy the local.settings.example.json file, rename it to local.settings.json, and fill it in with your MessageFlow credentials.

### In Azure Portal:

- Go to: App Service → Configuration → Environment variables
- Add two variables:
  - `RestApiAuthorization`
  - `RestApiApplicationKey`

---

## Running the function locally

1. Make sure you have .NET 8 SDK installed
2. Configure `local.settings.json` with valid API keys
3. Go to project dir
4. Run the function:

```bash
func start
```

<IMG>

---

## Publish and test your function

Follow these steps to publish your project:

- Clone the repository and navigate to the selected directory
- Open Visual Studio Code from your project's root directory. If prompted, select Yes, I trust the authors.
- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions: Create Function App in Azure.

Respond to the prompts as follows:

- If prompted, sign in to your Azure account.
- Select the Azure subscription to use. The prompt doesn't appear when you have only one subscription visible under Resources.
- Enter a globally unique name that's valid in a URL path. The name you enter is validated to make sure that it's unique in Azure Functions.
- Select an Azure region. For better performance, select a region near you.
- Select the runtime stack for your function app (select the language version you currently run locally, e.g., .NET 8 Isolated).
- Select resource authentication type. Select Managed identity, which is the most secure option for connecting to the default host storage account.

In the Azure: Activity Log panel, the Azure extension shows the status of individual resources as they're created in Azure.

<img>

# Deploy the project to Azure:

- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions: Deploy to Function App.
- Select the function app you just created. When prompted about overwriting previous deployments, select Deploy to deploy your function code to the new function app resource.
- When deployment is completed, select View Output to view the creation and deployment results, including the Azure resources that you created. If you miss the notification, select the bell icon in the lower-right corner to see it again.

Run the function in Azure

- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions:Execute Function Now...
- Select recently deployed function
- Copy and paste payload

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
- URL:
  - `https://your-function-url/api/MessageFlowEmailHttpTrigger`
  - `https://your-function-url/api/MessageFlowSmsHttpTrigger`
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

You can also copy the SmsService or EmailService file and use them in your code.

---

## Contributors

- Created by: MessageFlow
