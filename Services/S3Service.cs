using Amazon;
using Amazon.S3;
using Amazon.S3.Transfer;

namespace Dishora.Services
{
    public class S3Service
    {
        private readonly AmazonS3Client _s3Client;
        private readonly string _bucketName;
        private readonly string _region;

        public S3Service(IConfiguration config)
        {
            _bucketName = config["AWS:BucketName"];
            _region = config["AWS:Region"];

            // ⚠️ For now still hardcoding keys (but from config later you could use env vars)
            var accessKey = config["AWS:AccessKey"]; // Add this to appsettings if you want
            var secretKey = config["AWS:SecretKey"];

            _s3Client = new AmazonS3Client(accessKey, secretKey, RegionEndpoint.GetBySystemName(_region));
        }

        public async Task<string> UploadFileAsync(IFormFile file, string folderName = "uploads")
        {
            using var stream = file.OpenReadStream();
            var key = $"{folderName}/{Guid.NewGuid()}_{file.FileName}";

            var request = new TransferUtilityUploadRequest
            {
                InputStream = stream,
                Key = key,
                BucketName = _bucketName,
                ContentType = file.ContentType
            };

            var fileTransferUtility = new TransferUtility(_s3Client);
            await fileTransferUtility.UploadAsync(request);

            // Construct file URL
            return $"https://{_bucketName}.s3.{_region}.amazonaws.com/{key}";
        }
    }
}