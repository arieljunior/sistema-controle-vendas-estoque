using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace Scve.Infrastructure.Persistence;

public class ScveDbContextFactory : IDesignTimeDbContextFactory<ScveDbContext>
{
    public ScveDbContext CreateDbContext(string[] args) =>
        new(new DbContextOptionsBuilder<ScveDbContext>().UseSqlite("Data Source=design-time.db").Options);
}
