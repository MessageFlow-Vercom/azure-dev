using System.Text;
using System.Text.Json;

namespace SendEmailMessageflowApp.Services
{
    public class EmailService
    {
        private readonly HttpClient _httpClient;
        private readonly IConfiguration _configuration;
        private readonly ILogger<EmailService> _logger;

        public EmailService(HttpClient httpClient, IConfiguration configuration, ILogger<EmailService> logger)
        {
            _httpClient = httpClient;
            _configuration = configuration;
            _logger = logger;
        }

        public async Task<EmailResponse> SendEmailAsync(EmailRequest request)
        {
            try
            {
                // Get configuration values
                var authorization = _configuration["RestApi:Authorization"];
                var applicationKey = _configuration["RestApi:ApplicationKey"];
    
                if (string.IsNullOrEmpty(authorization) || string.IsNullOrEmpty(applicationKey))
                {
                    throw new InvalidOperationException("REST API configuration is missing. Please check Authorization and ApplicationKey settings.");
                }

                // Prepare headers
                _httpClient.DefaultRequestHeaders.Clear();
                _httpClient.DefaultRequestHeaders.Add("Authorization", authorization);
                _httpClient.DefaultRequestHeaders.Add("Application-Key", applicationKey);
                
                // Prepare request payload
                var payload = new
                {
                    subject = request.Subject,
                    smtpAccount = request.SmtpAccount,
                    tags = request.Tags,
                    content = request.Content,
                    bcc = request.Bcc,
                    cc = request.Cc,
                    from = request.From,
                    replyTo = request.ReplyTo,
                    headers = request.Headers,
                    globalVars = request.GlobalVars,
                    to = request.To,
                    attachments = request.Attachments
                };

                var jsonPayload = JsonSerializer.Serialize(payload, new JsonSerializerOptions
                {
                    PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
                    WriteIndented = true,
                    Encoder = System.Text.Encodings.Web.JavaScriptEncoder.UnsafeRelaxedJsonEscaping
                });

                // Log Payload
                _logger.LogInformation("Email API Request Payload: {Payload}", jsonPayload);

                // Create HTTP content
                var content = new StringContent(jsonPayload, Encoding.UTF8, "application/json");

                // Log Request details
                _logger.LogInformation("Sending HTTP request to: https://api.messageflow.com/v2.1/email");
                _logger.LogInformation("Request headers: Authorization={Auth}, Application-Key={Key}, Content-Type=application/json", 
                    authorization?.Substring(0, Math.Min(10, authorization?.Length ?? 0)) + "...", 
                    applicationKey?.Substring(0, Math.Min(10, applicationKey?.Length ?? 0)) + "...");
                
                HttpResponseMessage response;

                try
                {
                    response = await _httpClient.PostAsync("https://api.messageflow.com/v2.1/email", content);
                }
                catch (HttpRequestException ex) when (ex.InnerException is System.Security.Authentication.AuthenticationException)
                {
                    _logger.LogError(ex, "Connection failed. This might be due to invalid certificate or network issues.");

                    return new EmailResponse
                    {
                        Success = false,
                        Message = "SSL connection failed. Please check the API endpoint and network configuration.",
                        ResponseContent = ex.ToString()
                    };
                }
                catch (HttpRequestException ex)
                {
                    _logger.LogError(ex, "HTTP request failed");

                    return new EmailResponse
                    {
                        Success = false,
                        Message = $"HTTP request failed: {ex.Message}",
                        ResponseContent = ex.ToString()
                    };
                }
                
                var responseContent = await response.Content.ReadAsStringAsync();

                // Log Response
                _logger.LogInformation("Email API Response Status: {StatusCode}", response.StatusCode);
                _logger.LogInformation("Email API Response Content: {Content}", responseContent);

                // Parse Response
                try
                {
                    var apiResponse = JsonSerializer.Deserialize<EmailApiResponse>(responseContent, new JsonSerializerOptions
                    {
                        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
                    });

                    if (apiResponse?.Meta?.Status == 200)
                    {
                        return new EmailResponse
                        {
                            Success = true,
                            Message = $"Email sent successfully! Data count: {apiResponse.Meta.NumberOfData}",
                            ResponseContent = responseContent
                        };
                    }
                    else
                    {
                        var errorMessage = "Email sending failed";

                        if (apiResponse?.Errors?.Length > 0)
                        {
                            errorMessage = string.Join("; ", apiResponse.Errors.Select(e => $"{e.Title}: {e.Message}"));
                        }
                        
                        return new EmailResponse
                        {
                            Success = false,
                            Message = errorMessage,
                            ResponseContent = responseContent
                        };
                    }
                }
                catch (JsonException ex)
                {
                    _logger.LogError(ex, "Failed to parse API response");

                    return new EmailResponse
                    {
                        Success = false,
                        Message = "Failed to parse API response",
                        ResponseContent = responseContent
                    };
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending email");

                return new EmailResponse
                {
                    Success = false,
                    Message = $"Error: {ex.Message}",
                    ResponseContent = ex.ToString()
                };
            }
        }
    }

    public class EmailRequest
    {
        public string Subject { get; set; } = string.Empty;
        public string? SmtpAccount { get; set; }
        public string[]? Tags { get; set; }
        public EmailContent? Content { get; set; }
        public EmailRecipient[]? Bcc { get; set; }
        public EmailRecipient[]? Cc { get; set; }
        public EmailRecipient? From { get; set; }
        public EmailRecipient? ReplyTo { get; set; }
        public Dictionary<string, string>? Headers { get; set; }
        public Dictionary<string, object>? GlobalVars { get; set; }
        public EmailRecipient[] To { get; set; } = Array.Empty<EmailRecipient>();
        public EmailAttachment[]? Attachments { get; set; }
    }

    public class EmailContent
    {
        public string? Html { get; set; }
        public string? Text { get; set; }
        public string? TemplateId { get; set; }
    }

    public class EmailRecipient
    {
        public string Email { get; set; } = string.Empty;
        public string? Name { get; set; }
        public string? MessageId { get; set; }
        public Dictionary<string, object>? Vars { get; set; }
    }

    public class EmailAttachment
    {
        public string FileName { get; set; } = string.Empty;
        public string FileMime { get; set; } = string.Empty;
        public string FileContent { get; set; } = string.Empty;
        public bool Inline { get; set; } = false;
    }

    public class EmailResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public string ResponseContent { get; set; } = string.Empty;
    }

    // Email API Response Models

    public class EmailApiResponse
    {
        public EmailData[]? Data { get; set; }
        public EmailError[]? Errors { get; set; }
        public EmailMetadata? Meta { get; set; }
    }

    public class EmailData
    {
        public string? MessageId { get; set; }
        public string? Email { get; set; }
        public int Status { get; set; }
    }

    public class EmailError
    {
        public string? Title { get; set; }
        public string? Message { get; set; }
        public string? Code { get; set; }
    }

    public class EmailMetadata
    {
        public int NumberOfErrors { get; set; }
        public int NumberOfData { get; set; }
        public int Status { get; set; }
        public string? UniqId { get; set; }
    }
}
