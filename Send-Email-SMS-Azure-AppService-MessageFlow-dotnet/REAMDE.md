# 📬 Deploy an ASP.NET web app + MessageFlow API

Complete integration of email and SMS delivery using Azure ASP.NET web app and the MessageFlow API.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free/dotnet) with an active subscription.
- [Visual Studio Code](https://www.visualstudio.com/downloads)
- The [Azure Tools](https://marketplace.visualstudio.com/items?itemName=ms-vscode.vscode-node-azure-pack) extension.
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

### Locally (`appsettings.json`):

```json
{
  "RestApi": {
    "Authorization": "your-auth-key",
    "ApplicationKey": "your-app-key"
  }
}
```

You can copy the appsettings.example.json file, rename it to appsettings.json, and fill it in with your MessageFlow credentials.

### In Azure Portal:

- Go to: App Service → Configuration → Application Settings
- Add two variables:
  - `Authorization`
  - `ApplicationKey`

---

## Publish and test your web app

Follow these steps to create your App Service resources and publish your project:

- Clone the repository and navigate to the selected directory
- Open Visual Studio Code from your project's root directory. If prompted, select Yes, I trust the authors.
- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select Azure App Service: Create New Web App (Advanced).

Respond to the prompts as follows:

- If prompted, sign in to your Azure account.
- Select your Subscription.
- Select the region where you want to host your web app.
- Select hostname for your web app.
- Create an Azure resource group
- Select a unique name for your web app.
- Select the runtime stack for your web app (e.g., .NET 8 (LTS)).
- Select the system on which you want to run the application
- Select a pricing tier for your App Service Plan (or create a new one).

Once the web app is created, you will see a notification in Visual Studio Code.

After the web app is created, you can publish your project:

- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select Azure App Service: Deploy to Web App.

Respond to the prompts as follows:

- If prompted, sign in to your Azure account.
- Select the Subscription where your web app is hosted.
- Select the web app you created in the previous steps.
- Click the "Deploy" button when it appears.
- Visual Studio Code will build and deploy your project to the Azure App Service.
- Once the deployment is complete, you will see a notification in Visual Studio Code indicating that the deployment was successful.

When publishing completes, select Browse Website in the notification and select Open when prompted or in Visual Studio Code, select View > Command Palette to open the Command Palette and search for and select Azure App Service: Browse Website.

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
  - `https://your-function-url/email`
  - `https://your-function-url/sms`
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

You can also copy the SmsService or EmailService file and use them in your code.

---

## Contributors

- Created by: MessageFlow
