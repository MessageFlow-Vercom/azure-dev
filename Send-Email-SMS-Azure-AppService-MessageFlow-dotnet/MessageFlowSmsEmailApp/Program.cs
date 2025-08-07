using MessageFlowSmsEmailApp.Services;

var builder = WebApplication.CreateBuilder(args);

// Add Application Insights
builder.Services.AddApplicationInsightsTelemetry();

// Add HTTP client for SMS service
builder.Services.AddHttpClient<SmsService>();
builder.Services.AddScoped<SmsService>();

// Add HTTP client for Email service
builder.Services.AddHttpClient<EmailService>();
builder.Services.AddScoped<EmailService>();

var app = builder.Build();

// Configure the HTTP request pipeline for API only  
app.UseRouting();

// Root endpoint for health check
string healthMessage = "MessageFlow SMS/Email API app is running. Use POST /sms or POST /email endpoints.";

app.MapGet("/", () => healthMessage);
app.MapPost("/", () => healthMessage);

// SMS endpoint
app.MapPost("/sms", async (SmsRequest request, SmsService smsService) =>
{
    try
    {
        var result = await smsService.SendSmsAsync(request);
        return Results.Ok(result);
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message);
    }
});

// Email endpoint  
app.MapPost("/email", async (EmailRequest request, EmailService emailService) =>
{
    try
    {
        var result = await emailService.SendEmailAsync(request);
        return Results.Ok(result);
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message);
    }
});

app.Run();
