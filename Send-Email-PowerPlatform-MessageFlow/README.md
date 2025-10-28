# Integrate MessageFlow API with Power Apps (No Custom Connector)

This tutorial shows how to send emails using the MessageFlow REST API from an Power Apps without creating a custom connector. The integration uses a Power Automate flow triggered by PowerApps, which sends an HTTP POST request to the MessageFlow API.

---

## Requirements

- MessageFlow account ([Register here](https://app.messageflow.com/register))
- API key with appropriate permissions (email)
- An [Power Apps account](https://make.powerapps.com)
- An [Power Automate account](https://make.powerautomate.com)

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

### 4. Create the Flow

- Go to https://make.powerautomate.com
- Click "Create" from the left menu
- Select "Instant cloud flow"
- Name it "Send Email via MessageFlow REST API"
- Choose "PowerApps" as the trigger ("When Power Apps calls a flow (V2)")
- Click "Create"

---

### 5. Initialize variables

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

### 6. Add HTTP Request Action

- In the flow editor, click "Add a new step".
- Select the "HTTP" action.
- Configure the "HTTP" action as follows:

  - URI: `https://api.messageflow.com/v2.1/email`
  - Method: `POST`
  - Headers:
    - Authorization: `replace-with-your-messageflow-authorization`
    - Application-Key: `replace-with-your-messageflow-application-key`
    - Content-Type: `application/json`
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

    Then in each parameter press the “/” key between "", select “Insert dynamic content,” and choose the appropriate inputs from the previous step.

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
          "email": "@{triggerBody()?['email']}",
          "name": "@{triggerBody()?['text_2']}"
        }
      ],
      "content": {
        "html": "@{triggerBody()?['text_4']}",
        "text": "@{triggerBody()?['text_5']}"
      }
    }
    ```
    
    <img width="617" height="495" alt="img2" src="https://github.com/user-attachments/assets/ae458333-df34-4da6-825a-6c1d2fe1cef2" />

    > This payload includes only the required fields: subject, smtpAccount, from, to, and content. If you want to add more fields, check the endpoint documentation [here](https://dev.messageflow.com/emails#post-v2.1-email)

- On the Power Automate designer top toolbar, select "Save".

---

### 6. Parse the Response from API

- On the designer canvas, select the + icon and selct "Add an action"
- Search for "Parse JSON" and select it
- In "Content", press the “/” key, select “Insert dynamic content,” and choose "Body" from the "HTTP action"
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

### 7. Add Respond to a Power App

- On the designer canvas, select the + icon and selct "Add an action"
- Search for "Respond to a PowerApp or flow" and select it
- Click "Add an output" and add two outputs:

  - Name: `success`, Type: "Yes/No", Value: press the “/” key, select “Insert dynamic content,” and choose `success` from "Parse JSON"
  - Name: `message`, Type: "Text", Value: press the “/” key, select “Insert dynamic content,” and choose `message` from "Parse JSON"

  <img width="617" height="270" alt="img4" src="https://github.com/user-attachments/assets/9a3d87dc-0a71-4b68-8761-736ebd0b3529" />

- On the Power Automate designer top toolbar, select "Save".

---

### 8. Test the Flow

- Click "Test" in the top-right corner and choose "Manually".
- Provide sample values for each parameter to verify the flow works correctly.

---

## Contributors

- Created by: [MessageFlow](https://dev.messageflow.com)
