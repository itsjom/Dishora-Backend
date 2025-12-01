using MailKit.Net.Smtp;
using MailKit.Security;
using Microsoft.Extensions.Options;
using MimeKit;

namespace Dishora.Services
{
    public class EmailService : IEmailService
    {
        private readonly EmailSettings _settings;

        public EmailService(IOptions<EmailSettings> settings)
        {
            _settings = settings.Value;
        }

        public async Task SendEmailAsync(string toEmail, string subject, string htmlMessage)
        {
            var message = new MimeMessage();
            message.From.Add(new MailboxAddress("Dishora", _settings.Email));
            message.To.Add(MailboxAddress.Parse(toEmail));
            message.Subject = subject;
            message.Body = new TextPart("html")
            {
                Text = htmlMessage
            };

            using var client = new SmtpClient();
            await client.ConnectAsync(_settings.SmtpServer, _settings.Port, SecureSocketOptions.StartTls);
            await client.AuthenticateAsync(_settings.Email, _settings.Password);
            await client.SendAsync(message);
            await client.DisconnectAsync(true);
        }

        public async Task SendVerificationEmail(string email, string token)
        {
            var message = new MimeMessage();
            message.From.Add(new MailboxAddress("Dishora", _settings.Email));
            message.To.Add(MailboxAddress.Parse(email));
            message.Subject = "Verify Your Email - Dishora";

            string verificationLink = $"http://dishora-app-v2-env.eba-ti3cn8ta.ap-southeast-1.elasticbeanstalk.com/api/users/verify?token={token}";

            message.Body = new TextPart("html")
            {
                Text = $"<h3>Welcome!</h3><p>Please <a href='{verificationLink}'>click here</a> to verify your email.</p>"
            };

            using var client = new SmtpClient();
            await client.ConnectAsync(_settings.SmtpServer, _settings.Port, SecureSocketOptions.StartTls);
            await client.AuthenticateAsync(_settings.Email, _settings.Password);
            await client.SendAsync(message);
            await client.DisconnectAsync(true);
        }
    }
}
