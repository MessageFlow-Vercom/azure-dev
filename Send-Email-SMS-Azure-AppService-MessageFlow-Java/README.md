# Deploy a Java Spring Boot web app + MessageFlow API

Complete integration of email and SMS delivery using Azure Java Spring Boot web app and the MessageFlow API.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- An [Azure account](https://azure.microsoft.com/free/dotnet) with an active subscription.
- [Visual Studio Code](https://www.visualstudio.com/downloads) or [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Azure Tools extension pack](https://marketplace.visualstudio.com/items?itemName=ms-vscode.vscode-node-azure-pack) installed and be signed into Azure from VS Code.
- [Java 21](https://adoptium.net/) installed locally (Java 21 is recommended by Azure, Java 17+ supported)
- [Maven 3.8+](https://maven.apache.org/download.cgi) installed locally
- [Azure CLI](https://docs.microsoft.com/cli/azure/install-azure-cli) (optional, for command-line deployment)

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

### Locally (`application.properties`)

```properties
messageflow.api.authorization=your-authorization-key
messageflow.api.application-key=your-application-key
```

You can copy the application-example.properties file, rename it to application.properties, and fill it in with your MessageFlow credentials.

### Using Environment Variables

You can also use environment variables (recommended for production):

```bash
export MESSAGEFLOW_API_AUTHORIZATION="your-authorization-key"
export MESSAGEFLOW_API_APPLICATION_KEY="your-application-key"
```

Spring Boot will automatically map these to the `messageflow.api.authorization` and `messageflow.api.application-key` properties.

### In Azure Portal (Recommended for Production)

Azure App Service automatically provides the `PORT` environment variable. You need to add your MessageFlow credentials:

- Go to: App Service -> Settings -> Environment variables
- Add two variables (using Spring Boot property naming):
  - Name: `MESSAGEFLOW_API_AUTHORIZATION` -> Value: your-authorization-key
  - Name: `MESSAGEFLOW_API_APPLICATION_KEY` -> Value: your-application-key

**Important**:
- Spring Boot automatically converts environment variables with underscores to property names with dots and lowercase (e.g., `MESSAGEFLOW_API_AUTHORIZATION` -> `messageflow.api.authorization`)
- After adding configuration, restart your App Service for changes to take effect.

**Alternative**: You can also use the exact property names:
  - `messageflow.api.authorization` = your-authorization-key
  - `messageflow.api.application-key` = your-application-key

---

## Running the web app locally

To run the application locally (with Spring Boot):

1. Go to the application folder:

```bash
cd MessageFlowSmsEmailApp
```

2. Copy the example properties file and fill in your credentials:

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Edit `src/main/resources/application.properties` with your MessageFlow credentials.

3. Build the project:

```bash
mvn clean install
```

4. Run the app:

```bash
mvn spring-boot:run
```

Or run the JAR file directly:

```bash
java -jar target/messageflow-api.jar
```

5. Browse to the application at http://localhost:8080 in a web browser.

---

## Publish and test your web app

Follow these steps to create your App Service resources and publish your project:

### Option 1: Using Maven Plugin (Recommended - Matches Azure Documentation)

The `pom.xml` already includes the Azure Web App Maven Plugin configured for deployment.

> **Pricing Tier Note**: The default configuration uses the **F1 (Free)** tier, suitable for development and testing. For production deployments, Microsoft recommends using **P1V2** or **P1v3** pricing tiers. You can change this in the pom.xml file (line 102) by setting `<pricingTier>P1V2</pricingTier>`.

1. **Login to Azure** (if not already logged in):

```bash
az login
```

2. **Configure the plugin** (first time only):

```bash
cd MessageFlowSmsEmailApp
mvn com.microsoft.azure:azure-webapp-maven-plugin:2.14.1:config
```

Follow the prompts:
- Select your subscription
- For "Define value for javaVersion", type **1** for Java 21
- Choose Linux as OS
- Select Java SE as web container
- Confirm the configuration

> **Note**: The pom.xml already includes a pre-configured plugin setup with Java 21. You can skip this step if you want to use the default configuration (resource group: messageflow-rg, app name: messageflow-java-webapp, region: westeurope).

3. **Build and deploy**:

```bash
mvn clean package
mvn azure-webapp:deploy
```

4. **Configure Application Settings**:

After deployment, set your MessageFlow credentials in Azure Portal:
- Go to: App Service -> Configuration -> Application Settings
- Add:
  - `MESSAGEFLOW_API_AUTHORIZATION` = your-authorization-key
  - `MESSAGEFLOW_API_APPLICATION_KEY` = your-application-key
- Save and restart the app

5. **Test your app**:

```bash
# Get the app URL
az webapp show --name messageflow-java-webapp --resource-group messageflow-rg --query defaultHostName --output tsv

# Or open in browser
az webapp browse --name messageflow-java-webapp --resource-group messageflow-rg
```

### Option 2: Using VS Code

- Clone the repository and navigate to the selected directory
- Open Visual Studio Code from your project's root directory. If prompted, select Yes, I trust the authors.
- Locate the Azure icon in the left-hand toolbar. Select it to bring up the Azure Tools for VS Code extension.

> If you do not see the Azure Tools icon, make sure you have the Azure Tools extension for VS Code installed.

In the Azure Tools extension for VS Code:

- Find the RESOURCES section and select your subscription.
- Select + (Create Resource...)
- Choose the "Create App Service Web App..." option.
- Select the region where you want to host your web app.
- Enter the name messageflow-java-webapp-XYZ for this web app, where XYZ is any three unique characters.
- Select the runtime stack: **Java 21** (recommended) or Java 17
- Select the Java web server stack: **Java SE** (for standalone JAR)
- Select the App Service plan (pricing tier) for this web app.

Deploy the application:
- Build the project: `mvn clean package`
- Right-click on messageflow-java-webapp-XYZ web app and select "Deploy to Web App..."
- Select the `target/messageflow-api.jar` file to deploy
- When prompted, confirm deployment
- When the deployment is complete, click "Browse Website"

**Configure Application Settings**:
After deployment, go to Azure Portal and add your MessageFlow credentials as described in the "Secure Credential Storage" section.

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
[Azure]
      ↓
[Spring Boot App - Email / SMS]
      ↓
[MessageFlow API]
      ↓
[Client Response]
```

---

## Your own solution

You can also copy the SmsService or EmailService classes and use them in your code.

---

## Project Structure

```
MessageFlowSmsEmailApp/
├── pom.xml                                          # Maven configuration
├── .gitignore                                       # Git ignore rules
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── messageflow/
        │           └── api/
        │               ├── MessageFlowApplication.java    # Main Spring Boot app
        │               ├── controllers/
        │               │   └── MessageFlowController.java # REST endpoints
        │               ├── services/
        │               │   ├── SmsService.java           # SMS service
        │               │   └── EmailService.java         # Email service
        │               └── models/
        │                   ├── SmsRequest.java
        │                   ├── SmsResponse.java
        │                   ├── EmailRequest.java
        │                   ├── EmailResponse.java
        │                   ├── EmailRecipient.java
        │                   └── EmailContent.java
        └── resources/
            ├── application.properties                     # Configuration (not in git)
            └── application-example.properties             # Example configuration
```

---

## Available Maven Commands

- `mvn clean install` - Build the project
- `mvn spring-boot:run` - Run the application locally
- `mvn test` - Run tests
- `mvn clean package` - Create JAR file
- `mvn azure-webapp:config` - Configure Azure Web App (first time)
- `mvn azure-webapp:deploy` - Deploy to Azure App Service

---

## Dependencies

- **Spring Boot 3.2.0**: Modern Java framework
- **Spring Web**: REST API support
- **Jackson**: JSON serialization/deserialization
- **SLF4J/Logback**: Logging framework
- **RestTemplate**: HTTP client for API calls
- **Spring Boot Actuator**: Health checks and monitoring

---

## Contributors

- Created by: [MessageFlow](https://dev.messageflow.com)
