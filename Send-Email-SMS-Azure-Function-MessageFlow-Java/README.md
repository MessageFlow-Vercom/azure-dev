# Deploy a Java function app + MessageFlow API

Complete integration of email and SMS delivery using Azure Java function app and the MessageFlow API.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free/dotnet) with an active subscription.
- [Visual Studio Code](https://www.visualstudio.com/downloads)
- The [Azure Functions extension](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurefunctions) for Visual Studio Code, version 1.8.1 or later.
- [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) for Visual Studio Code
- [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local)
- [Java Development Kit (JDK)](https://learn.microsoft.com/en-us/java/openjdk/download) version 8, 11, 17, or 21.
- [Apache Maven](https://maven.apache.org/) version 3.0 or higher.

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

> **Important:** The application loads configuration in the following priority order:
> 1. Environment variables (recommended for both local and Azure deployment)
> 2. `config.json` file (fallback for non-Azure scenarios)

### Locally (`local.settings.json`) - Recommended:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "RestApi__ApplicationKey": "your-application-key",
    "RestApi__Authorization": "your-authorization-key"
  }
}
```

You can copy the `local.settings.example.json` file, rename it to `local.settings.json`, and fill it in with your MessageFlow credentials. Azure Functions Core Tools automatically loads these values as environment variables during local development.

### Alternative: Using `config.json` (Fallback):

```json
{
  "rest_api": {
    "application_key": "your-messageflow-application-key-here",
    "authorization": "your-messageflow-authorization-token-here"
  }
}
```

You can copy the `config.example.json` file, rename it to `config.json`, and fill it in with your MessageFlow credentials. This method is provided as a fallback but is not the standard Azure Functions approach.

### In Azure Portal (Production):

- Go to: Function App -> Settings -> Environment variables
- Add two variables with your MessageFlow credentials:
  - `RestApi__ApplicationKey`
  - `RestApi__Authorization`

---

## Running the function locally

To run the application locally:

1. Install Azure Functions Core Tools and Maven
2. Go to function directory (MessageFlowEmailFunction or MessageFlowSmsFunction)
3. Configure credentials (choose one method):

   **Option A: Using local.settings.json (Recommended for local development)**

   ```bash
   cp local.settings.example.json local.settings.json
   ```

   Edit `local.settings.json` with your MessageFlow credentials.

   **Option B: Using config.json**

   ```bash
   cp config.example.json config.json
   ```

   Edit `config.json` with your MessageFlow credentials.

4. Build and run function:
   ```bash
   mvn clean package
   mvn azure-functions:run
   ```

The function will start locally and listen on `http://localhost:7071/api/MessageFlowEmailHttpTrigger` (or `MessageFlowSmsHttpTrigger` for SMS).

---

## Publish and test your function

Follow these steps to publish your project:

- Clone the repository and navigate to the selected function directory
- Open Visual Studio Code from your project's root directory. If prompted, select Yes, I trust the authors.
- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions: Create Function App in Azure.

Respond to the prompts as follows:

- If prompted, sign in to your Azure account.
- Select the Azure subscription to use. The prompt doesn't appear when you have only one subscription visible under Resources.
- Enter a globally unique name that's valid in a URL path. The name you enter is validated to make sure that it's unique in Azure Functions.
- Select an Azure region. For better performance, select a region near you.
- Select the runtime stack for your function app (select the language version you currently run locally.
).
- Select resource authentication type. Select Managed identity, which is the most secure option for connecting to the default host storage account.

In the Azure: Activity Log panel, the Azure extension shows the status of individual resources as they're created in Azure.

### Deploy project to Azure:

You can deploy using either VS Code or Maven:

**Option A: Using VS Code**

- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions: Deploy to Function App.
- Select the function app you just created. When prompted about overwriting previous deployments, select Deploy to deploy your function code to the new function app resource.
- When deployment is completed, select View Output to view the creation and deployment results, including the Azure resources that you created.

**Option B: Using Maven**

> **Note:** Before deploying with Maven, you should update the `pom.xml` configuration to match your Azure resources:
> - `<appName>` - Your function app name (or keep the default with timestamp)
> - `<resourceGroup>` - Your Azure resource group name (default: `java-functions-group`)
> - `<appServicePlanName>` - Your app service plan name (default: `java-functions-app-service-plan`)
> - `<region>` - Your preferred Azure region (default: `westeurope`)

```bash
mvn azure-functions:deploy
```

This will package and deploy your function to Azure using the configuration in pom.xml.

### Configure Application Settings in Azure

After deployment, you must configure your MessageFlow credentials:

1. In Azure Portal, navigate to your Function App
2. Go to **Configuration** -> **Application settings**
3. Click **+ New application setting** and add:
   - Name: `RestApi__Authorization`, Value: your-authorization-key
   - Name: `RestApi__ApplicationKey`, Value: your-application-key
4. Click **Save** and then **Continue** to restart the function app

### Run the function in Azure

- In Visual Studio Code, select View > Command Palette to open the Command Palette.
- Search for and select: Azure Functions: Execute Function Now...
- Select recently deployed function
- Copy and paste payload (see examples below)

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
  - Choose `raw -> JSON`
  - Paste a sample payload

---

## Data Flow Diagram

```
[Client Request]
      ↓
[Azure Function]
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

### Email Function

```
MessageFlowEmailFunction/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── messageflow/
│                   └── function/
│                       ├── EmailFunction.java         # HTTP trigger function
│                       ├── models/
│                       │   ├── EmailRecipient.java
│                       │   ├── EmailContent.java
│                       │   ├── EmailRequest.java
│                       │   └── EmailResponse.java
│                       ├── services/
│                       │   ├── EmailService.java      # Email service implementation
│                       │   └── NullExclusionStrategy.java
│                       └── utils/
│                           └── Config.java            # Configuration loader
├── pom.xml                                            # Maven configuration
├── host.json                                          # Function app settings
├── local.settings.example.json                        # Example local settings
├── config.example.json                                # Example config file
└── .gitignore                                         # Git ignore rules
```

### SMS Function

```
MessageFlowSmsFunction/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── messageflow/
│                   └── function/
│                       ├── SmsFunction.java           # HTTP trigger function
│                       ├── models/
│                       │   ├── SmsRequest.java
│                       │   └── SmsResponse.java
│                       ├── services/
│                       │   ├── SmsService.java        # SMS service implementation
│                       │   └── NullExclusionStrategy.java
│                       └── utils/
│                           └── Config.java            # Configuration loader
├── pom.xml                                            # Maven configuration
├── host.json                                          # Function app settings
├── local.settings.example.json                        # Example local settings
├── config.example.json                                # Example config file
└── .gitignore                                         # Git ignore rules
```

---

## Available Maven Commands

- `mvn clean package` - Build the project
- `mvn azure-functions:run` - Run the function locally
- `mvn azure-functions:deploy` - Deploy to Azure
- `mvn test` - Run tests

---

## Dependencies

- **azure-functions-java-library**: Azure Functions SDK for Java
- **okhttp**: Modern HTTP client for making API requests
- **gson**: JSON serialization/deserialization library

---

## Contributors

- Created by: [MessageFlow](https://dev.messageflow.com)
