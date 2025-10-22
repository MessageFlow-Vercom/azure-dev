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

<img width="1059" height="675" alt="" src="https://github.com/user-attachments/assets/c42f419e-d2b4-4e43-b975-0cf2526e305e" />

- On the "Add a trigger" screen under Built-in tools, select Request, and on the next screen select When a HTTP request is received.

<img width="631" height="393" alt="" src="https://github.com/user-attachments/assets/99aa91e0-eee1-4d39-9d99-3180d2f9055c" />
<img width="631" height="393" alt="" src="https://github.com/user-attachments/assets/10ce3f12-237f-4d0b-96ef-65a86c0b99d9" />

The trigger appears on the designer canvas.

- On the When a HTTP request is received screen, select "Use sample payload to generate schema" below the "Request Body JSON Schema" text field

<img width="1036" height="605" alt="" src="https://github.com/user-attachments/assets/70a6fc5b-0ee6-4fa0-b457-e79818a6348c" />

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

<img width="628" height="417" alt="" src="https://github.com/user-attachments/assets/daa81dbc-588f-4fb5-a020-0a8b9a5ad9ca" />

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

<img width="1277" height="789" alt="" src="https://github.com/user-attachments/assets/c260717d-86d7-4ed8-a121-4d4ae0ab3d93" />

- Close "Parameters" screen
- On the Logic App designer top toolbar, select "Save".

---

### 6. Add a API call

- On the designer canvas, select the + icon and selct "Add an action"

<img width="349" height="167" alt="" src="https://github.com/user-attachments/assets/a264becc-2f6d-4c9b-802c-b71042c3630e" />

- On the "Add an action" screen select "Http".

<img width="620" height="450" alt="" src="https://github.com/user-attachments/assets/3fffd310-81f8-4626-9dd8-2b2b7d101913" />

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
<img width="1277" height="642" alt="headers  1" src="https://github.com/user-attachments/assets/565433d5-990c-4aa4-8762-6a52f31e5fbb" />
<img width="1277" height="642" alt="headers 2" src="https://github.com/user-attachments/assets/adba6cd7-c5bb-4e49-ba00-c134eb8284cd" />
<img width="621" height="272" alt="body" src="https://github.com/user-attachments/assets/47028923-8515-444f-a41f-63e1f7133de1" />

  

- On the Logic App designer top toolbar, select Save.

---

### 7. Add a Response

- On the designer canvas, select the + icon and selct "Add an action" and select "Request" -> "Response".

<img width="869" height="366" alt="" src="https://github.com/user-attachments/assets/98547512-6a53-484f-8ec0-34f0ec64db7e" />

- In "Status Code" field press “/”, select the “Insert dynamic content” button, and select "Status Code"
- Do the same with the body field

<img width="881" height="448" alt="" src="https://github.com/user-attachments/assets/82d032d5-52a7-4ed6-a928-cc0a5838df23" />

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

<img width="590" height="558" alt="" src="https://github.com/user-attachments/assets/becd27ee-1437-4111-b3b7-a8552aa89431" />

---

## Contributors

- Created by: MessageFlow
