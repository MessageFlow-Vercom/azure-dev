using System.Text;
using System.Text.Json;

namespace SendSmsMessageflowApp.Services
{
    public class SmsService
    {
        private readonly HttpClient _httpClient;
        private readonly IConfiguration _configuration;
        private readonly ILogger<SmsService> _logger;

        public SmsService(HttpClient httpClient, IConfiguration configuration, ILogger<SmsService> logger)
        {
            _httpClient = httpClient;
            _configuration = configuration;
            _logger = logger;
        }

        public async Task<SmsResponse> SendSmsAsync(SmsRequest request)
        {
            try
            {
                // Get configuration values
                var authorization = _configuration["RestApi:Authorization"];
                var applicationKey = _configuration["RestApi:ApplicationKey"];
                
                _logger.LogInformation("Configuration - Authorization: {Auth}, ApplicationKey: {Key}", 
                    authorization?.Substring(0, Math.Min(10, authorization?.Length ?? 0)) + "...", 
                    applicationKey?.Substring(0, Math.Min(10, applicationKey?.Length ?? 0)) + "...");

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
                    sender = request.Sender,
                    message = request.Message,
                    phoneNumbers = request.PhoneNumbers,
                    validity = request.Validity,
                    scheduleTime = request.ScheduleTime,
                    type = request.Type,
                    shortLink = request.ShortLink,
                    webhookUrl = request.WebhookUrl,
                    externalId = request.ExternalId
                };

                var jsonPayload = JsonSerializer.Serialize(payload, new JsonSerializerOptions
                {
                    PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
                    WriteIndented = true
                });

                // Log the payload
                _logger.LogInformation("SMS API Request Payload: {Payload}", jsonPayload);

                // Create HTTP content
                var content = new StringContent(jsonPayload, Encoding.UTF8, "application/json");

                // Log request details
                _logger.LogInformation("Sending HTTP request to: https://api.messageflow.com/v2.1/sms");
                _logger.LogInformation("Request headers: Authorization={Auth}, Application-Key={Key}, Content-Type=application/json", 
                    authorization?.Substring(0, Math.Min(10, authorization?.Length ?? 0)) + "...", 
                    applicationKey?.Substring(0, Math.Min(10, applicationKey?.Length ?? 0)) + "...");
                
                HttpResponseMessage response;
             
                // Send request to real API
                _logger.LogInformation("Attempting to send request to API...");

                try
                {
                    response = await _httpClient.PostAsync("https://api.messageflow.com/v2.1/sms", content);
                }
                catch (HttpRequestException ex) when (ex.InnerException is System.Security.Authentication.AuthenticationException)
                {
                    _logger.LogError(ex, "Connection failed. This might be due to invalid certificate or network issues.");

                    return new SmsResponse
                    {
                        Success = false,
                        Message = "SSL connection failed. Please check the API endpoint and network configuration.",
                        ResponseContent = ex.ToString()
                    };
                }
                catch (HttpRequestException ex)
                {
                    _logger.LogError(ex, "HTTP request failed");

                    return new SmsResponse
                    {
                        Success = false,
                        Message = $"HTTP request failed: {ex.Message}",
                        ResponseContent = ex.ToString()
                    };
                }
                
                var responseContent = await response.Content.ReadAsStringAsync();

                // Log the response
                _logger.LogInformation("SMS API Response Status: {StatusCode}", response.StatusCode);
                _logger.LogInformation("SMS API Response Content: {Content}", responseContent);

                // Parse response
                try
                {
                    var apiResponse = JsonSerializer.Deserialize<SmsApiResponse>(responseContent, new JsonSerializerOptions
                    {
                        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
                    });

                    if (apiResponse?.Meta?.Status == 200)
                    {
                        return new SmsResponse
                        {
                            Success = true,
                            Message = $"SMS sent successfully! Sent: {apiResponse.Meta.SentSms}, Not sent: {apiResponse.Meta.NotSentSms}",
                            ResponseContent = responseContent
                        };
                    }
                    else
                    {
                        var errorMessage = "SMS sending failed";

                        if (apiResponse?.Errors?.Length > 0)
                        {
                            errorMessage = string.Join("; ", apiResponse.Errors.Select(e => $"{e.Title}: {e.Message}"));
                        }
                        
                        return new SmsResponse
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

                    return new SmsResponse
                    {
                        Success = false,
                        Message = "Failed to parse API response",
                        ResponseContent = responseContent
                    };
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending SMS");

                return new SmsResponse
                {
                    Success = false,
                    Message = $"Error: {ex.Message}",
                    ResponseContent = ex.ToString()
                };
            }
        }
    }

    public class SmsRequest
    {
        public string Sender { get; set; } = string.Empty;
        public string Message { get; set; } = string.Empty;
        public string[] PhoneNumbers { get; set; } = Array.Empty<string>();
        public int? Validity { get; set; }
        public long? ScheduleTime { get; set; }
        public int? Type { get; set; }
        public bool? ShortLink { get; set; }
        public string? WebhookUrl { get; set; }
        public string? ExternalId { get; set; }
    }

    public class SmsResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public string ResponseContent { get; set; } = string.Empty;
    }

    // API Response Models
    public class SmsApiResponse
    {
        public SmsData[]? Data { get; set; }
        public SmsError[]? Errors { get; set; }
        public SmsMetadata? Meta { get; set; }
    }

    public class SmsData
    {
        public string? Sender { get; set; }
        public string? PhoneNumber { get; set; }
        public string? ExternalId { get; set; }
        public int Status { get; set; }
        public long StatusTime { get; set; }
        public long CreateTime { get; set; }
        public int SmsPrice { get; set; }
        public int Currency { get; set; }
        public int NumberOfParts { get; set; }
    }

    public class SmsError
    {
        public string? Title { get; set; }
        public string? Message { get; set; }
        public string? Code { get; set; }
        public SmsErrorMeta? Meta { get; set; }
    }

    public class SmsErrorMeta
    {
        public string? Source { get; set; }
        public string? Parameter { get; set; }
        public string? Value { get; set; }
    }

    public class SmsMetadata
    {
        public int SentSms { get; set; }
        public int NotSentSms { get; set; }
        public string? AccountBalance { get; set; }
        public int NumberOfErrors { get; set; }
        public int NumberOfData { get; set; }
        public int Status { get; set; }
        public string? UniqId { get; set; }
    }
}
