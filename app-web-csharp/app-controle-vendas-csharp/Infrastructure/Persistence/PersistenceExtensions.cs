using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;

namespace Scve.Infrastructure.Persistence;

public static class PersistenceExtensions
{
    public static IServiceCollection AddPersistence(
        this IServiceCollection services, IConfiguration configuration, IHostEnvironment environment)
    {
        var configured = configuration.GetConnectionString("Default")
            ?? throw new InvalidOperationException("Connection string 'Default' não encontrada.");

        var connection = new SqliteConnectionStringBuilder(configured);
        connection.DataSource = Path.GetFullPath(connection.DataSource, environment.ContentRootPath);
        Directory.CreateDirectory(Path.GetDirectoryName(connection.DataSource)!);

        return services.AddDbContext<ScveDbContext>(o => o.UseSqlite(connection.ToString()));
    }
}
