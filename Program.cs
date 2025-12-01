using Amazon.S3;
using Amazon.SimpleNotificationService;
using Dishora;
using Dishora.Data;
using Dishora.Hubs;
using Dishora.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// ============================
// 🔑 1. Services registration
// ============================

// Controllers + Swagger
builder.Services.AddControllers().AddJsonOptions(options =>
{
    options.JsonSerializerOptions.ReferenceHandler =
        System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;
});
builder.Services.AddScoped<PayMongoSignatureFilter>();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        In = ParameterLocation.Header,
        Description = "Please insert JWT with Bearer into field",
        Name = "Authorization",
        Type = SecuritySchemeType.ApiKey,
        Scheme = "Bearer"
    });

    c.AddSecurityRequirement(new OpenApiSecurityRequirement {
        {
            new OpenApiSecurityScheme {
                Reference = new OpenApiReference {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
});

// Email Service
builder.Services.Configure<EmailSettings>(builder.Configuration.GetSection("EmailSettings"));
builder.Services.AddScoped<IEmailService, EmailService>();
builder.Services.AddScoped<EmailService>();

// PayMongo Service (with HttpClient + DI)
builder.Services.AddHttpClient<PayMongoService>();
builder.Services.AddScoped<PayMongoService>();

// 🔑 Load AWS Options (from appsettings.json or environment variables)
builder.Services.AddDefaultAWSOptions(builder.Configuration.GetAWSOptions());

// S3 Service
builder.Services.AddAWSService<IAmazonS3>();
builder.Services.AddScoped<S3Service>();

// Pre-Order and Order Service
builder.Services.AddScoped<IPreOrderService, PreOrderService>();
builder.Services.AddScoped<IOrderService, OrderService>();
builder.Services.AddScoped<IPaymentProcessingService, PaymentProcessingService>(); // <-- ADD THIS

// AWS SNS (Notification)
builder.Services.AddAWSService<IAmazonSimpleNotificationService>();
builder.Services.AddScoped<INotificationService, NotificationService>();

// SignalR services
builder.Services.AddSignalR();

// Tell SignalR how to find the User ID in your JWT
// This tells SignalR to use the 'nameidentifier' claim (standard for user ID)
builder.Services.AddSingleton<IUserIdProvider, NameUserIdProvider>();

// Allow CORS (mobile/ReactNative/Angular clients will need this)
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader());
});

// Add Authentication + Authorization (JWT Bearer)
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        // 👇 belongs on JwtBearerOptions, *outside* TokenValidationParameters
        options.RequireHttpsMetadata = false;

        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,

            ValidIssuer = builder.Configuration["Jwt:Issuer"],
            ValidAudience = builder.Configuration["Jwt:Audience"],
            IssuerSigningKey = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(builder.Configuration["Jwt:Key"]))
        };
    });
builder.Services.AddAuthorization();

builder.Services.AddDbContext<DishoraDbContext>(options =>
    options
      .UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection"))
      .EnableSensitiveDataLogging()        // 👈 reveals values
      .EnableDetailedErrors()              // 👈 richer exceptions
);

// Build pipeline

var app = builder.Build();

// Swagger UI in Dev environments
app.UseSwagger();
app.UseSwaggerUI();

app.UseDeveloperExceptionPage(); // Feel free to disable in prod

// app.UseHttpsRedirection();

// CORS for mobile/web requests
app.UseCors("AllowAll");

// Authentication + Authorization
app.UseAuthentication(); // ✅ must come before UseAuthorization
app.UseAuthorization();

app.UseStaticFiles();

// Map the endpoint for your ChatHub
// This tells the server to listen for SignalR connections at "https://yourdomain.com/chathub"
app.MapHub<ChatHub>("/chathub");

// Map Controllers
app.MapControllers();

app.Run();