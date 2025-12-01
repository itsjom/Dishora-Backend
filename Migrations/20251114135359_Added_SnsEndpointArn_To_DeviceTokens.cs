using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Dishora.Migrations
{
    /// <inheritdoc />
    public partial class Added_SnsEndpointArn_To_DeviceTokens : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "sns_endpoint_arn",
                table: "device_tokens",
                type: "nvarchar(max)",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "sns_endpoint_arn",
                table: "device_tokens");
        }
    }
}
