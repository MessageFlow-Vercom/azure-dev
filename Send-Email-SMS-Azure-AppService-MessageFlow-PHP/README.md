# Deploy a PHP web app + MessageFlow API

Complete integration of email and SMS delivery using Azure PHP web app and the MessageFlow API.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free/) with an active subscription.
- [Visual Studio Code](https://www.visualstudio.com/downloads)
- [Azure Tools extension pack](https://marketplace.visualstudio.com/items?itemName=ms-vscode.vscode-node-azure-pack) installed and be signed into Azure from VS Code.
- [PHP 8.1 or higher](https://www.php.net/downloads) installed locally.
- [Composer](https://getcomposer.org/) (required for autoloading)

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

The application supports two configuration methods:

### 1. Local Development (`config.json`):

For local development, create a `config.json` file:

```json
{
  "rest_api": {
    "application_key": "your-application-key",
    "authorization": "your-authorization-key"
  }
}
```

You can copy the config.example.json file, rename it to config.json, and fill it in with your MessageFlow credentials.

### 2. Azure Production (Application Settings):

To configure credentials in Azure:

- Go to: App Service -> Settings -> Environment variables
- Add two environment variables:
  - **Name:** `ApplicationKey` **Value:** your MessageFlow application key
  - **Name:** `Authorization` **Value:** your MessageFlow authorization token
- Click **Save** and wait for the app to restart

**How it works:**

- In Azure: The app reads credentials from environment variables (`ApplicationKey` and `Authorization`)
- Locally: The app falls back to `config.json` if environment variables are not set
- The app automatically detects the environment using Azure's `WEBSITE_SITE_NAME` variable

---

## Running the web app locally

To run the application locally with PHP built-in server:

1. Go to the application folder:

```bash
cd MessageFlowSmsEmailApp
```

2. Copy the example configuration:

```bash
cp config.example.json config.json
```

3. Edit `config.json` and add your MessageFlow credentials.

4. Install Composer dependencies:

```bash
composer install --no-dev
```

5. Start the PHP built-in web server:

```bash
php -S localhost:8000 -t .
```

6. Browse to the application at http://localhost:8000 in a web browser.

> For production deployments, consider using a proper web server like Apache or Nginx with PHP-FPM.

---

## Publish and test your web app

Follow these steps to create your App Service resources and publish your project:

- Clone the repository and navigate to the selected directory
- Open Visual Studio Code from your project's root directory. If prompted, select Yes, I trust the authors.
- Locate the Azure icon in the left-hand toolbar. Select it to bring up the Azure Tools for VS Code extension.

> If you do not see the Azure Tools icon, make sure you have the Azure Tools extension for VS Code installed.

In the Azure Tools extension for VS Code:

- Find the RESOURCES section and select your subscription.
- Select + (Create Resource...)
- Choose the "Create App Service Web App..." option.
- Select the region where you want to host your web app.
- Enter the name `messageflow-php-webapp-XYZ` for this web app, where XYZ is any three unique characters. When deployed, this name is used as your app name.
- Select the runtime stack for the application. In this project, select **PHP 8.2** or **PHP 8.3**.
- Select the App Service plan (pricing tier) for this web app. The App Service plan controls how many resources (CPU/memory) are available to your app and how much you pay.
- Select the **Linux** operating system (PHP on Windows is no longer supported as of November 2022).

Once created:

- Go to the RESOURCES section and select your subscription.
- Inside App Services right click on `messageflow-php-webapp-XYZ` web app and select "Deploy to web app..."
- Select the `MessageFlowSmsEmailApp` folder as the one to deploy.
- Select resource (`messageflow-php-webapp-XYZ` for this web app)
- When the deployment is complete, a notification will appear in the lower right corner of VS Code. You can use this notification to browse to your web app (click "Browse Website" button)

A web page will open, and if everything worked correctly, you will see the message:

```
{"message":"MessageFlow SMS/Email API app is running. Use POST /sms or POST /email endpoints."}
```

> You can copy the web page URL and use it during testing with Postman

### Configure Application Settings in Azure

After deployment, you need to add your MessageFlow credentials to Azure:

1. Go to your App Service in the Azure Portal
2. Navigate to: Settings -> Environment variables
3. Add the following settings:
   - **Name:** `ApplicationKey` **Value:** your MessageFlow application key
   - **Name:** `Authorization` **Value:** your MessageFlow authorization token
4. Click **Save** and wait for the app to restart

**Verification:**
After saving, browse to your app URL. You should see:

```json
{
  "message": "MessageFlow SMS/Email API app is running. Use POST /sms or POST /email endpoints.",
  "environment": "Azure App Service",
  "php_version": "8.2.x",
  "timestamp": "2025-01-05T10:30:00+00:00"
}
```

If you see `"Service configuration unavailable"` error, check that Application Settings are configured correctly.

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
  - Choose `raw -> JSON`
  - Paste a sample payload

### Optional: Environment Variables in Postman

- Add `Authorization` and `ApplicationKey` as environment variables
- Use them as `{{Authorization}}` and `{{ApplicationKey}}`

---

## Data Flow Diagram

```
[Client Request]
      ↓
[Azure App Service - PHP]
      ↓
[index.php - Routes & Validation]
      ↓
[EmailService / SmsService]
      ↓
[MessageFlow API]
      ↓
[Client Response]
```

## PHP Requirements

The application requires:

- PHP 8.1 or higher
- Composer (for PSR-4 autoloading)
- cURL extension (for HTTP requests)
- JSON extension (for data handling)

These extensions are typically enabled by default in Azure App Service.

**Note:** The application uses Composer's PSR-4 autoloader. Azure App Service automatically runs `composer install` during deployment if `composer.json` is detected.

---

## Your own solution

You can also copy the SmsService.php or EmailService.php files and use them in your code. Both services are standalone classes that only require:

- A configuration array with `rest_api.authorization` and `rest_api.application_key`
- PHP with cURL and JSON extensions

---

## Troubleshooting

1. **"REST API authorization is missing in configuration"**

   - In Azure: Add `Authorization` and `ApplicationKey` to Application Settings
   - Locally: Verify `config.json` has the correct structure with `rest_api` object

2. **Environment Detection Issues**
   - Check the health endpoint response - it shows whether app detected Azure or Local environment
   - Azure detection uses the `WEBSITE_SITE_NAME` environment variable

---

## Contributors

- Created by: [MessageFlow](https://dev.messageflow.com)
