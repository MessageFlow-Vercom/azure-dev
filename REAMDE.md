# 📬 Azure Functions + MessageFlow API

Complete integration of email and SMS delivery using Azure Functions and the MessageFlow API.

---

## 🧰 Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (SMS, email)
- Azure Functions Core Tools / local environment / Azure Portal
- .NET 6 or later
- Postman (for testing)

---

## 🚀 Quick Start

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

> ⚠️ Keep your credentials secure — they will be needed in code!

---

## 📂 Repository Structure

Repository: [github.com/MessageFlow-Vercom/azure-dev](https://github.com/MessageFlow-Vercom/azure-dev)

| Folder         | Description                                                         |
| -------------- | ------------------------------------------------------------------- |
| `AppService.*` | Classes for sending emails and SMS using the API                    |
| `Function.*`   | Azure Functions handling HTTP requests and using AppService classes |

---

## ✉️ Example — Email Function

```csharp
[FunctionName("SendEmailFunction")]
public async Task<IActionResult> Run(
    [HttpTrigger(AuthorizationLevel.Function, "post", Route = null)] HttpRequest req,
    ILogger log)
{
    var body = await new StreamReader(req.Body).ReadToEndAsync();
    var requestData = JsonConvert.DeserializeObject<EmailRequest>(body);

    var sender = new EmailSender(
        Environment.GetEnvironmentVariable("Authorization"),
        Environment.GetEnvironmentVariable("ApplicationKey"));

    var result = await sender.SendEmailAsync(requestData);
    return new OkObjectResult(result);
}
```

---

## 📱 Example — SMS Function

```csharp
[FunctionName("SendSmsFunction")]
public async Task<IActionResult> Run(
    [HttpTrigger(AuthorizationLevel.Function, "post", Route = null)] HttpRequest req,
    ILogger log)
{
    var body = await new StreamReader(req.Body).ReadToEndAsync();
    var requestData = JsonConvert.DeserializeObject<SmsRequest>(body);

    var sender = new SmsSender(
        Environment.GetEnvironmentVariable("Authorization"),
        Environment.GetEnvironmentVariable("ApplicationKey"));

    var result = await sender.SendSmsAsync(requestData);
    return new OkObjectResult(result);
}
```

---

## 🔐 Secure Credential Storage

### Locally (`local.settings.json`):

```json
{
  "Values": {
    "Authorization": "your-auth-key",
    "ApplicationKey": "your-app-key"
  }
}
```

### In Azure Portal:

- Go to: App Service → Configuration → Application Settings
- Add two variables:
  - `Authorization`
  - `ApplicationKey`

---

## 📦 Sample JSON Payloads

### Email

```json
{
  "smtpAccount": "1.example.smtp",
  "subject": "Welcome to MessageFlow!",
  "to": [
    { "email": "user1@example.com", "name": "User 1" },
    { "email": "user2@example.com", "name": "User 2" }
  ],
  "cc": [{ "email": "cc@example.com", "name": "CC User" }],
  "bcc": [{ "email": "bcc@example.com", "name": "BCC User" }],
  "content": {
    "html": "<h1>Hello!</h1><p>This is a test email from Azure Functions.</p><p>Thanks for your attention!</p>",
    "text": "Hello! This is a test email from Azure Functions. Thanks for your attention!"
  },
  "from": {
    "email": "daniel@db.freshmail.it",
    "name": "Azure Functions Team"
  },
  "replyTo": {
    "email": "reply@example.com",
    "name": "Reply Handler"
  },
  "tags": ["azure", "functions", "test"],
  "headers": {
    "X-Custom-Header": "CustomValue",
    "X-Priority": "High"
  },
  "globalVars": {
    "company": "Microsoft",
    "environment": "development"
  }
}
```

### SMS

```json
{
  "sender": "senderName",
  "message": "Lorem ipsum",
  "phoneNumbers": ["+48123456789"],
  "externalId": "test-sms-001"
}
```

---

## 🧪 Testing with Postman

### Request Setup:

- Method: `POST`
- URL:
  - `https://your-function-url/api/SendEmailFunction`
  - `https://your-function-url/api/SendSmsFunction`
- Headers:
  - `Content-Type: application/json`
- Body:
  - Choose `raw → JSON`
  - Paste a sample payload

### Optional: Environment Variables in Postman

- Add `Authorization` and `ApplicationKey` as environment variables
- Use them as `{{Authorization}}` and `{{ApplicationKey}}`

---

## 📊 Data Flow Diagram

```
[Client Request]
      ↓
[Azure Function]
      ↓
[AppService.EmailSender / SmsSender]
      ↓
[MessageFlow API]
      ↓
[Client Response]
```

---

## 👥 Contributors

- Created by: MessageFlow
- Repository: [github.com/MessageFlow-Vercom/azure-dev](https://github.com/MessageFlow-Vercom/azure-dev)
