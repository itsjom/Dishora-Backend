using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace Dishora.Data
{
    public class DishoraDbContextFactory : IDesignTimeDbContextFactory<DishoraDbContext>
    {
        public DishoraDbContext CreateDbContext(string[] args)
        {
            var config = new ConfigurationBuilder()
                .SetBasePath(Directory.GetCurrentDirectory())
                .AddJsonFile("appsettings.json")
                .Build();

            var optionsBuilder = new DbContextOptionsBuilder<DishoraDbContext>();
            optionsBuilder.UseSqlServer(config.GetConnectionString("DefaultConnection"));

            return new DishoraDbContext(optionsBuilder.Options);
        }
    }
}
