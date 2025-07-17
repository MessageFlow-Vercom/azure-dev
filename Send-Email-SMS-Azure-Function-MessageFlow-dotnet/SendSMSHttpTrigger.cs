using Microsoft.Extensions.Configuration;
using System.Text.Json;
using System.Net.Http;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;
using System.IO;
using System.Linq;
using System.Net;
using System;
using SmsService = MessageFlowAzureWebApp.Services.SmsService;
using SmsRequest = MessageFlowAzureWebApp.Services.SmsRequest;
using SmsResponse = MessageFlowAzureWebApp.Services.SmsResponse;

namespace MessageFlowAzureWebApp.Functions
{
    public class SendSMSHttpTrigger
    {
        private readonly ILogger<SendSMSHttpTrigger> _logger;

        public SendSMSHttpTrigger(ILogger<SendSMSHttpTrigger> logger)
        {
            _logger = logger;
        }

        [Function("SendSMSHttpTrigger")]
        public async Task<HttpResponseData> Run(
            [HttpTrigger(AuthorizationLevel.Anonymous, "post")] HttpRequestData req,
            FunctionContext executionContext)
        {
            string requestBody = await new StreamReader(req.Body).ReadToEndAsync();

            _logger.LogWarning($"Raw request body: {requestBody}");

            SmsRequest? smsRequest = JsonSerializer.Deserialize<SmsRequest>(
                requestBody,
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true }
            );

            _logger.LogWarning($"Deserialized smsRequest: {JsonSerializer.Serialize(smsRequest)}");

            if (smsRequest == null)
            {
                var badResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                await badResponse.WriteStringAsync("Invalid request body.");

                return badResponse;
            }

            if (string.IsNullOrWhiteSpace(smsRequest.Sender) ||
                string.IsNullOrWhiteSpace(smsRequest.Message) ||
                smsRequest.PhoneNumbers == null || smsRequest.PhoneNumbers.Length == 0)
            {
                var badResponse = req.CreateResponse(HttpStatusCode.BadRequest);
                await badResponse.WriteStringAsync("Missing required fields: sender, message, phoneNumbers.");
                return badResponse;
            }

            // get Authorization and Application-Key
            string? headerAuthorization = req.Headers.TryGetValues("Authorization", out var authValues) ? authValues.FirstOrDefault() : null;
            string? headerApplicationKey = req.Headers.TryGetValues("Application-Key", out var appKeyValues) ? appKeyValues.FirstOrDefault() : null;
            
            if (!string.IsNullOrEmpty(headerAuthorization))
            {
                smsRequest.HeaderAuthorization = headerAuthorization;
            }
            if (!string.IsNullOrEmpty(headerApplicationKey))
            {
                smsRequest.HeaderApplicationKey = headerApplicationKey;
            }

            var config = executionContext.InstanceServices.GetService(typeof(IConfiguration)) as IConfiguration;
            var loggerFactory = executionContext.InstanceServices.GetService(typeof(ILoggerFactory)) as ILoggerFactory;

            ILogger<SmsService> logger;

            if (loggerFactory != null)
            {
                logger = loggerFactory.CreateLogger<SmsService>();
            }
            else
            {
                logger = _logger as ILogger<SmsService> ?? throw new InvalidCastException("Cannot cast _logger to ILogger<SmsService>");
            }

            var httpClient = new HttpClient();
            var smsService = new SmsService(httpClient, config!, logger);
            
            SmsResponse response = await smsService.SendSmsAsync(smsRequest);

            var okResponse = req.CreateResponse(HttpStatusCode.OK);

            okResponse.Headers.Add("Content-Type", "application/json");

            await okResponse.WriteStringAsync(JsonSerializer.Serialize(response));

            return okResponse;
        }
    }
}
