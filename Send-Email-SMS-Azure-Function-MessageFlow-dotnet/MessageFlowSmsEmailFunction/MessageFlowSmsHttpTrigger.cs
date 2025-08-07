using Microsoft.Azure.Functions.Worker;
using Microsoft.Extensions.Logging;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using MessageFlowSmsEmailFunc.Services;
using System.Text.Json;

namespace MessageFlowSmsEmailFunc;

public class MessageFlowSmsHttpTrigger
{
    private readonly ILogger<MessageFlowSmsHttpTrigger> _logger;
    private readonly SmsService _smsService;

    public MessageFlowSmsHttpTrigger(ILogger<MessageFlowSmsHttpTrigger> logger, SmsService smsService)
    {
        _logger = logger;
        _smsService = smsService;
    }

    [Function("MessageFlowSmsHttpTrigger")]
    public async Task<IActionResult> Run([HttpTrigger(AuthorizationLevel.Anonymous, "post")] HttpRequest req)
    {
        _logger.LogInformation("C# HTTP trigger function processed a SMS request.");

        try
        {
            // Read request body
            string requestBody = await new StreamReader(req.Body).ReadToEndAsync();

            if (string.IsNullOrEmpty(requestBody))
            {
                return new BadRequestObjectResult("Request body is empty");
            }

            // Deserialize request
            var smsRequest = JsonSerializer.Deserialize<SmsRequest>(requestBody, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            });

            if (smsRequest == null)
            {
                return new BadRequestObjectResult("Invalid request format");
            }

            // Validate required fields
            if (string.IsNullOrEmpty(smsRequest.Sender) ||
                string.IsNullOrEmpty(smsRequest.Message) ||
                smsRequest.PhoneNumbers == null ||
                smsRequest.PhoneNumbers.Length == 0)
            {
                return new BadRequestObjectResult("Missing required fields: sender, message, and phoneNumbers are required");
            }

            _logger.LogInformation("Sending SMS to {PhoneCount} numbers from {Sender}",
                smsRequest.PhoneNumbers.Length, smsRequest.Sender);

            // Send SMS using SmsService
            var result = await _smsService.SendSmsAsync(smsRequest);

            if (result.Success)
            {
                _logger.LogInformation("SMS sent successfully: {Message}", result.Message);

                return new OkObjectResult(new
                {
                    success = true,
                    message = result.Message,
                    responseContent = result.ResponseContent
                });
            }
            else
            {
                _logger.LogError("Failed to send SMS: {Message}", result.Message);

                return new ObjectResult(new
                {
                    success = false,
                    message = result.Message,
                    responseContent = result.ResponseContent
                })
                {
                    StatusCode = 500
                };
            }
        }
        catch (JsonException ex)
        {
            _logger.LogError(ex, "Failed to parse request JSON");

            return new BadRequestObjectResult("Invalid JSON format in request body");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unexpected error occurred while processing SMS request");

            return new ObjectResult(new
            {
                success = false,
                message = "An unexpected error occurred",
                error = ex.Message
            })
            {
                StatusCode = 500
            };
        }
    }
}
