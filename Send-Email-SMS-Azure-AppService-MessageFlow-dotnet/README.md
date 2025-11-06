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

### Locally (`appsettings.json`):

```json
{
  "RestApi": {
    "ApplicationKey": "your-app-key",
    "Authorization": "your-auth-key"
  }
}
```

You can copy the appsettings.example.json file, rename it to appsettings.json, and fill it in with your MessageFlow credentials.

### In Azure Portal:

- Go to: App Service → Configuration → Application Settings
- Add two variables:
  - `ApplicationKey`
  - `Authorization`

---

## Running the web app locally

To run the application locally:

1. Go to the application directory:
2. Run app:

```
dotnet run
```

<img width="1224" height="155" alt="Zrzut ekranu 2025-08-7 o 14 00 58" src="https://github.com/user-attachments/assets/899044c6-232f-460a-b459-1b735f04b4f7" />

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

<img width="1230" height="124" alt="Zrzut ekranu 2025-08-7 o 13 54 26" src="https://github.com/user-attachments/assets/2b440f19-7b42-4ac7-b16a-f72e89983832" />

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

<img width="1230" height="190" alt="Zrzut ekranu 2025-08-7 o 13 54 33" src="https://github.com/user-attachments/assets/3f0c04a2-522c-402c-ac70-7aa68e980c3f" />

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
