using Microsoft.Azure.Functions.Worker;
using Microsoft.Extensions.Logging;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using MessageFlowSmsEmailFunc.Services;
using System.Text.Json;

namespace MessageFlowSmsEmailFunc;

public class MessageFlowEmailHttpTrigger
{
    private readonly ILogger<MessageFlowEmailHttpTrigger> _logger;
    private readonly EmailService _emailService;

    public MessageFlowEmailHttpTrigger(ILogger<MessageFlowEmailHttpTrigger> logger, EmailService emailService)
    {
        _logger = logger;
        _emailService = emailService;
    }

    [Function("MessageFlowEmailHttpTrigger")]
    public async Task<IActionResult> Run([HttpTrigger(AuthorizationLevel.Anonymous, "post")] HttpRequest req)
    {
        _logger.LogInformation("C# HTTP trigger function processed an Email request.");

        try
        {
            // Read request body
            string requestBody = await new StreamReader(req.Body).ReadToEndAsync();

            if (string.IsNullOrEmpty(requestBody))
            {
                return new BadRequestObjectResult("Request body is empty");
            }

            // Deserialize request
            var emailRequest = JsonSerializer.Deserialize<EmailRequest>(
                requestBody,
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true }
            );

            if (emailRequest == null)
            {
                return new BadRequestObjectResult("Invalid request format");
            }

            // Validate required fields
            if (string.IsNullOrEmpty(emailRequest.Subject) ||
                emailRequest.To == null ||
                emailRequest.To.Length == 0 ||
                emailRequest.Content == null)
            {
                return new BadRequestObjectResult("Missing required fields: subject, to, and content are required");
            }

            _logger.LogInformation("Sending Email to {RecipientCount} recipients with subject: {Subject}",
                emailRequest.To.Length, emailRequest.Subject);

            // Send Email using EmailService
            var result = await _emailService.SendEmailAsync(emailRequest);

            if (result.Success)
            {
                _logger.LogInformation("Email sent successfully: {Message}", result.Message);
                return new OkObjectResult(new
                {
                    success = true,
                    message = result.Message,
                    responseContent = result.ResponseContent
                });
            }
            else
            {
                _logger.LogError("Failed to send Email: {Message}", result.Message);

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
            _logger.LogError(ex, "Unexpected error occurred while processing Email request");

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
