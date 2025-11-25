# Integrate MessageFlow API with Power Apps Canvas App and Azure Function

This tutorial shows how to send emails using the MessageFlow REST API and Azure Function from an Power Apps Canvas App.

---

## 🔗 Azure Marketplace

If you are deploying MessageFlow through Azure Marketplace, you can find our Email API offer here:

👉 **MessageFlow – Email API for Developers** ([See more here](https://marketplace.microsoft.com/en-us/product/saas/vercom.email-api-for-developers?tab=Overview))

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (email)
- Deployed [Azure Function](https://portal.azure.com) (if you want to add access restrictions, secure them with [Entra ID](https://entra.microsoft.com))
- An [Power Apps account](https://make.powerapps.com)
- An [Power Automate account](https://make.powerautomate.com)

---

## Data Flow Diagram

```
[Client Request]
      ↓
[PowerApps Canvas App]
      ↓
[Power Automate Flow]
      ↓
[Azure Function (secured via Entra ID)]
      ↓
[Client Response]
```

---

## Quick Start

### 1. Create a MessageFlow Account

- Register: [https://app.messageflow.com/register](https://app.messageflow.com/register)
- Confirm email and phone number
- Fill in company details and select services (Email)

### 2. Generate an API Key

- Navigate to the "Account" -> "Settings" -> "API" tab
- Click "New API Key"
- Name the key and set permissions, you need to select options "Transactional e-mail"
- Save the following values:
  - `ApplicationKey`
  - `Authorization`

> Keep your credentials secure — they will be needed in code!

> If you received "Creating API keys impossible until account approved by customer service office" error it might be that your account has not yet been activated. If this process takes longer than 3 hours, please contact our support team to verify your identity.

---

### 3. Additional requirements

To send emails we require two more things:

- domain in the FROM field must be verified. You can do this in "Email"->"Common settings"->"Senders authorization". More information you can read [here](https://docs.messageflow.com/technical-support-center/senders-authorization/how-to-authorize-senders-in-messageflow/domains-authorization)
- your personal "smtpAccount", you can get it from the MessageFlow application. Navigate to "Email" -> "Email API" -> "Settings" -> "SMTP Accounts"

---

> If you want to add access restrictions to your Azure Function, proceed to the next step and secure it with Entra ID. If not, proceed to "Create the Flow" step.

### 4. Secure Azure Function with Entra ID

- Go to [Microsoft Entra Admin Center](https://entra.microsoft.com)
- In the left menu, select "Applications" -> "App registrations"
- On the next screen click "+ New registration"
  - Fill in:
    - Name: `MessageFlowFunctionApp`
    - Supported account types: `Accounts in this organizational directory only (Single tenant)`
    - Redirect URI: Leave blank (not needed for backend APIs)
    - Click Register. After registration, copy:
      - Application (client) ID
      - Directory (tenant) ID

#

- In the App Registration, go to "Expose an API" in the left menu
- Click "Add" next to "Application ID URI"

  - Use: `api://<tenant-id>/<app-id>`

    - Example: `api://72f988bf-86f1-41af-91ab-2d7cd011db47/12345678-abcd-1234-abcd-1234567890ab`
    - Example: `api://<verified-domain>/<app-name>` - `api://example.com/messageflow-function` (only if `example.com` is a verified domain in your tenant)
    - Example: `api://<tenant-id>/messageflow-function`

    > You can find your "tenant ID" and "app ID" on the App Registration's Overview page.

  - Click Save

- Click "+ Add a scope" below
  - Scope name: `email.send`
  - Admin consent display name: `Send email via Azure Function`
  - Admin consent description: `Allows sending email using the secured Azure Function`
  - State: `Enabled`
  - Click `Add scope`

#

- Go to "API permissions"
- Click "+ Add a permission"
- Choose `Microsoft Graph` -> `Delegated permissions`
- Add `User.Read` (or other relevant permissions)
- Click "Add permission"
- Click "Click Grant admin consent for..." - next to "+ Add a permission" button

---

### 5. Enable Authentication in Azure Function

- Go to [Azure Portal](https://portal.azure.com)
- Open your Function App overview
- In the left menu, select "Settings" -> "Authentication"
- Click "+ Add identity provider"
  - Provider: "Microsoft (Sign in Microsoft and Microsoft Entra identities and call Microsoft APIs)"
  - Choose a tenant for your application and its users: "Workforce configuration (current tenant)"
  - App registration type: "Pick an existing app registration in this directory"
  - Name or app ID: slect your Azure Function App
  - Client secret expiration: choose what you prefer
  - Client application requirement: "Allow requests only from this application itself"
  - Identity Requirement: "Allow requests from any identity"
  - Tenant requirement: "Allow requests only from the issuer tenant"
  - Restrict Access: "Require authentication"
  - Unauthenticated Requests: "HTTP 401 Unauthorized: recommended for APIs"
  - Token store: `enable`
- Click "Add"

---

### 6. Create the Flow

- Go to https://make.powerautomate.com
- Click "Create" from the left menu
- Select "Instant cloud flow"
- Name it "Send Email via MessageFlow Azure Function"
- Choose "PowerApps" as the trigger ("When Power Apps calls a flow (V2)")
- Click "Create"

---

### 7. Initialize variables

- In the flow editor, click on "When Power Apps calls a flow (V2)" action
- Select the "Initialize variable" action.

- Add text inputs:

  - Name: `subject`, Type: `Text`
  - Name: `smtpAccount`, Type: `Text`
  - Name: `fromEmail`, Type: `Email`
  - Name: `fromName`, Type: `Text`
  - Name: `toEmail`, Type: `Email`
  - Name: `toName`, Type: `Text`
  - Name: `htmlContent`, Type: `Text`
  - Name: `textContent`, Type: `Text`

    <img width="625" height="601" alt="img1" src="https://github.com/user-attachments/assets/add451a4-d24e-4b15-bec9-e15dea5c9c9f" />

    > This paameters list includes only the required fields: subject, smtpAccount, from, to, and content. If you want to add more fields, check the endpoint documentation [here](https://dev.messageflow.com/emails#post-v2.1-email)

- On the Power Automate designer top toolbar, select "Save".

---

### 8. Add HTTP Request Action

- In the flow editor, click "Add a new step".
- Select the "HTTP" action.
- Configure the "HTTP" action as follows:

  - URI: `https://your-azure-function-url.azure.com/email`
  - Method: `POST`
  - Body:

    ```json
    {
      "subject": "",
      "smtpAccount": "",
      "from": {
        "email": "",
        "name": ""
      },
      "to": [
        {
          "email": "",
          "name": ""
        }
      ],
      "content": {
        "html": "",
        "text": ""
      }
    }
    ```

    Then in each parameter press the "/" key between "", select "Insert dynamic content" and choose the appropriate inputs from the previous step.

    or copy and paste this payload:

    ```json
    {
      "subject": "@{triggerBody()?['text']}",
      "smtpAccount": "@{triggerBody()?['text_1']}",
      "from": {
        "email": "@{triggerBody()?['email']}",
        "name": "@{triggerBody()?['text_2']}"
      },
      "to": [
        {
          "email": "@{triggerBody()?['email_1']}",
          "name": "@{triggerBody()?['text_3']}"
        }
      ],
      "content": {
        "html": "@{triggerBody()?['text_4']}",
        "text": "@{triggerBody()?['text_5']}"
      }
    }
    ```

    <img width="617" height="496" alt="img2_poprawione" src="https://github.com/user-attachments/assets/15f9f50f-f0e1-468a-865a-00c7dbf1e5fc" />

    > This payload includes only the required fields: subject, smtpAccount, from, to, and content. If you want to add more fields, check the endpoint documentation [here](https://dev.messageflow.com/emails#post-v2.1-email)

- On the Power Automate designer top toolbar, select "Save".

---

### 9. Parse the Response from API

- On the designer canvas, select the + icon and selct "Add an action"
- Search for "Parse JSON" and select it
- In "Content", press the "/" key, select "Insert dynamic content" and choose "Body" from the "HTTP action"
- In "Schema" click "Use sample payload to generate schema", and paste this sample response:

  ```json
  {
    "success": true,
    "message": "Email sent successfully",
    "response_content": {
      "uniqId": "12345",
      "status": "200"
    }
  }
  ```

  <img width="617" height="652" alt="img3" src="https://github.com/user-attachments/assets/7f5ba0d0-e9d6-4cd3-9a2a-212aaa052cb5" />

- On the Power Automate designer top toolbar, select "Save".

---

### 10. Add Respond to a Power App

- On the designer canvas, select the + icon and selct "Add an action"
- Search for "Respond to a PowerApp or flow" and select it
- Click "Add an output" and add two outputs:

  - Name: `success`, Type: "Yes/No", Value: press the "/" key, select "Insert dynamic content" and choose `success` from "Parse JSON"
  - Name: `message`, Type: "Text", Value: press the "/" key, select "Insert dynamic content" and choose `message` from "Parse JSON"

  <img width="617" height="270" alt="img4" src="https://github.com/user-attachments/assets/9a3d87dc-0a71-4b68-8761-736ebd0b3529" />

- On the Power Automate designer top toolbar, select "Save".

---

### 11. Test the Flow

- Click "Test" in the top-right corner and choose "Manually".
- Provide sample values for each parameter to verify the flow works correctly.

<img width="276" height="369" alt="tests" src="https://github.com/user-attachments/assets/06ebceb3-c9f0-4c7e-b7a8-38be5714783a" />

---

### 12. Configure Canvas App

- Go to https://make.powerapps.com

If you don't have the Canvas App yet:

- Click "Create" from the left menu
- Select "Start with a blank canvas"
- Select size for your Canvas App

If you already have an application open your Canvas App

- In the top menu, click "Add data" or in the menu on the left "Data sources" -> "Add Data"
- Search for "Power Apps for Makers" and select your connection

> If you don't see a connection to your Power Apps, make sure you are working in the same environment as Power Apps - check "Environment" on the right side of the top bar.

- In the screen on the right, click "Connect"

##

- Insert a button or use an existing one and go to its "Properties" -> "Advanced" (on the right side of the screen) or use the text field in the top menu - select `OnSelect` and paste:

```json
    If(
        Set(
            varEmailResponse;
            SendEmailviaAzureFunction.Run(
                "Test email from Canvas App";
                "1.test.smtp";
                "emailfrom@example.com";
                "Sender email name";
                "emailto@example.com";
                "Recipient email name";
                "email html content";
                "email text content"
            )
        );
        varEmailResponse.success;
        Notify("ok"; NotificationType.Success);
        Notify("error"; NotificationType.Error)
    )
```

> Note that differences in notation, particularly regarding the value separator (comma or semicolon), are mainly due to the regional settings of the operating system. For example, in some countries, the separator is a semicolon rather than a comma.

or add the appropriate fields in the application and insert their values.

- Click the "Save" icon in the upper right corner
- Click the "Play" icon next to it to test your application.

---

## Contributors

- Created by: [MessageFlow](https://dev.messageflow.com)

## About MessageFlow

MessageFlow is a developer-friendly communication platform designed to help teams deliver secure, high-quality transactional messaging at scale.

With our Email API you can:

- Send fast, reliable transactional emails from any system
- Track delivery, errors and engagement in real time
- Improve deliverability with verified domains and optimized infrastructure
- Simplify integration with transparent pricing and predictable performance.