using Microsoft.Extensions.Configuration;
using System.Text.Json;
using System.Net.Http;
using System.Threading.Tasks;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;
using System.IO;
using System.Linq;
using System.Net;
using System;
using EmailService = MessageFlowAzureWebApp.Services.EmailService;
using EmailRequest = MessageFlowAzureWebApp.Services.EmailRequest;
using EmailResponse = MessageFlowAzureWebApp.Services.EmailResponse;

namespace MessageFlowAzureWebApp.Functions
{
    public class SendEmailHttpTrigger
    {
        private readonly ILogger<SendEmailHttpTrigger> _logger;

        public SendEmailHttpTrigger(ILogger<SendEmailHttpTrigger> logger)
        {
            _logger = logger;
        }

        [Function("SendEmailHttpTrigger")]
        public async Task<HttpResponseData> Run(
            [HttpTrigger(AuthorizationLevel.Anonymous, "post")] HttpRequestData req,
            FunctionContext executionContext)
        {
            string requestBody = await new StreamReader(req.Body).ReadToEndAsync();

            _logger.LogWarning($"Raw request body: {requestBody}");

            EmailRequest? emailRequest = JsonSerializer.Deserialize<EmailRequest>(
                requestBody,
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true }
            );

            _logger.LogWarning($"Deserialized emailRequest: {JsonSerializer.Serialize(emailRequest)}");

            if (emailRequest == null)
            {
                var badResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                await badResponse.WriteStringAsync("Invalid request body.");
                return badResponse;
            }

            if (string.IsNullOrWhiteSpace(emailRequest.Subject) ||
                emailRequest.To == null || emailRequest.To.Length == 0 ||
                (emailRequest.Content == null || (string.IsNullOrWhiteSpace(emailRequest.Content.Html) && string.IsNullOrWhiteSpace(emailRequest.Content.Text))))
            {
                var badResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                await badResponse.WriteStringAsync("Missing required fields: subject, to, content (html or text).");
                return badResponse;
            }

            // get Authorization and Application-Key
            string? headerAuthorization = req.Headers.TryGetValues("Authorization", out var authValues) ? authValues.FirstOrDefault() : null;
            string? headerApplicationKey = req.Headers.TryGetValues("Application-Key", out var appKeyValues) ? appKeyValues.FirstOrDefault() : null;

            if (!string.IsNullOrEmpty(headerAuthorization))
            {
                emailRequest.HeaderAuthorization = headerAuthorization;
            }
            if (!string.IsNullOrEmpty(headerApplicationKey))
            {
                emailRequest.HeaderApplicationKey = headerApplicationKey;
            }

            var config = executionContext.InstanceServices.GetService(typeof(IConfiguration)) as IConfiguration;
            var loggerFactory = executionContext.InstanceServices.GetService(typeof(ILoggerFactory)) as ILoggerFactory;
           
            ILogger<EmailService> logger;
           
            if (loggerFactory != null)
            {
                logger = loggerFactory.CreateLogger<EmailService>();
            }
            else
            {
                logger = _logger as ILogger<EmailService> ?? throw new InvalidCastException("Cannot cast _logger to ILogger<EmailService>");
            }

            var httpClient = new HttpClient();
            var emailService = new EmailService(httpClient, config!, logger);

            EmailResponse response = await emailService.SendEmailAsync(emailRequest);

            var okResponse = req.CreateResponse(HttpStatusCode.OK);

            okResponse.Headers.Add("Content-Type", "application/json");

            await okResponse.WriteStringAsync(JsonSerializer.Serialize(response));

            return okResponse;
        }
    }
}
