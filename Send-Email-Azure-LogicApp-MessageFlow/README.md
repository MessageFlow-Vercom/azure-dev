# Integrate MessageFlow API with Azure Logic App

This tutorial shows how to send emails using the MessageFlow REST API from an Azure Logic App.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (email)
- An [Azure Logic Apps account](https://azure.microsoft.com/en-us/products/logic-apps) with permission to create resources

---

## Quick Start

### 1. Create a MessageFlow Account

- Register: [https://app.messageflow.com/register](https://app.messageflow.com/register)
- Confirm email and phone number
- Fill in company details and select services (Email)

### 2. Generate an API Key

- Navigate to the “Account”->“Settings”->“API” tab
- Click “New API Key”
- Name the key and set permissions, you need to select options “Transactional e-mail”
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

---

### 4. Create the logic app

- In Azure Logic App click "+ Add"
- Choose "Consumption" type
- Enter name, select resource group and choose a location
- Click "Create" to deploy your Logic App

---

### 5. Add the trigger

- Select "Add a trigger" on the logic app designer canvas.

IMG

- On the "Add a trigger" screen under Built-in tools, select Request, and on the next screen select When a HTTP request is received.

IMG

The trigger appears on the designer canvas.

- On the When a HTTP request is received screen, select "Use sample payload to generate schema" below the "Request Body JSON Schema" text field

IMG

- Paste the following code in the Enter or paste a sample JSON payload screen, and then select Done.

```json
{
  "subject": "Welcome to MessageFlow!",
  "email": "user1@example.com",
  "content": {
    "html": "<h1>Hello!</h1><p>This is a test email from Azure.</p><p>Thanks for your attention!</p>",
    "text": "Hello! This is a test email from Azure. Thanks for your attention!"
  }
}
```

You can also use more parameters depending on your needs, but you will have to configure them correctly in the “Http” screen (next step).

```json
{
  "subject": "Welcome to MessageFlow!",
  "to": [
    {
      "email": "user1@example.com",
      "name": "User Name 1"
    },
    {
      "email": "user2@example.com",
      "name": "User Name 2"
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
  "replyTo": {
    "email": "reply@example.com",
    "name": "Reply Handler"
  },
  "tags": ["azure", "logicapps", "test"],
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

Azure generates the schema for the request data you entered. In practice, you can capture the actual request data from your application code and use it to generate the JSON schema.

- On the Logic App designer top toolbar, select "Save".

- The generated HTTP URL now appears under HTTP URL on the "When a HTTP request is received" screen. Select the copy icon to copy the URL to use later.

IMG

---

### 5. Add Parameters

- On the Logic App designer top toolbar, select "Parameters".

- On the "Parameters" screen click "Create parameter". We need to add 4 parameters required by the MessageFlow API.

      - Authorization:
          - Name: Authorization
          - Type: String
          - Default value: YourAuthorizationKey (your MessageFlow credentials from step 2)

      - Application-Key:
          - Name: Application-Key
          - Type: String
          - Default value: YourApplicationKey (your MessageFlow credentials from step 2)

      - smtpAccount:
          - Name: smtpAccount
          - Type: String
          - Default value: please sign in to the MessageFlow application and navigate to “Email”->“Email API”->“Settings”->“SMTP Accounts”

      - from:
          - Name: smtpAccount
          - Type: Object
          - Default value: please sign in to the MessageFlow application and navigate to “Email”->“Common settings”->“Senders authorization” then copy "From address" and "From name" to the json below and paste it into the "Default value" field.

          ```json
          {
              "email": "From address",
              "name": "From name"
          }
          ```

  IMG

- Close "Parameters" screen
- On the Logic App designer top toolbar, select "Save".

---

### 6. Add a API call

- On the designer canvas, select the + icon and selct "Add an action"

IMG

- On the "Add an action" screen select "Http".

IMG

- In the configuration screen, enter:

  - url: https://api.messageflow.com/v2.1/email
  - method: select “POST”
  - Headers:

    - click in the text field and type “Authorization”, then click in the field next to it, press “/”, select the “Insert dynamic content” button, and select ‘Authorization’ from the “Parameters” category.
    - do the same for the "Application-Key" field
    - add field "Content-Type" with value "application/json"

  - Body: paste the following code:

  ```json
  {
      "subject": @{triggerBody()?['subject']},
      "smtpAccount": @{parameters('smtpAccount')},
      "to": [
          {
          "email": @{triggerBody()?['email']}
          }
      ],
      "content": @{triggerBody()?['content']},
      "from": @{parameters('from')}
  }
  ```

  IMG IMG

- On the Logic App designer top toolbar, select Save.

---

### 7. Add a Response

- On the designer canvas, select the + icon and selct "Add an action" and select "Request" -> "Response".

IMG

- In "Status Code" field press “/”, select the “Insert dynamic content” button, and select "Status Code"
- Do the same with the body field

IMG

- On the Logic App designer top toolbar, select Save.

---

### 8. Testing

- On the Logic App designer top toolbar, select "Run" -> "Run with payload".
- Paste the following sample JSON payload in "Body" field, and then click "Run".

```json
{
  "subject": "Welcome to MessageFlow!",
  "email": "user1@example.com",
  "content": {
    "html": "<h1>Hello!</h1><p>This is a test email from Azure.</p><p>Thanks for your attention!</p>",
    "text": "Hello! This is a test email from Azure. Thanks for your attention!"
  }
}
```

You should see the result of the query to the MessageFlow API along with the response code and its content.

IMG

---

## Contributors

- Created by: MessageFlow
