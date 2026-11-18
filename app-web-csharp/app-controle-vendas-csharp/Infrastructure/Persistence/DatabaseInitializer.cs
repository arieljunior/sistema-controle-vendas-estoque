using Microsoft.EntityFrameworkCore;
using Scve.Domain.Entities;
using Scve.Domain.Enums;

namespace Scve.Infrastructure.Persistence;

public static class DatabaseInitializer
{
    public static async Task InitializeDatabaseAsync(this WebApplication app)
    {
        await using var scope = app.Services.CreateAsyncScope();
        var db = scope.ServiceProvider.GetRequiredService<ScveDbContext>();

        await db.Database.MigrateAsync();

        if (app.Configuration.GetValue<bool>("Database:SeedSampleData") && !await db.Products.AnyAsync())
        {
            db.Products.AddRange(
                new Product(Guid.Parse("1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01"), "Chocolate", ProductType.Massa, 12.50m, 7),
                new Product(Guid.Parse("2c7a1d2f-4b6e-4d8f-8e22-1b2c3d4e5f02"), "Morango", ProductType.Picole, 5.00m, 5),
                new Product(Guid.Parse("3d8b2e30-5c7f-4e90-9f33-2c3d4e5f6a03"), "Baunilha", ProductType.Massa, 10.00m, 0));
            await db.SaveChangesAsync();
        }
    }
}
