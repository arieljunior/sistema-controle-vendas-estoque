using Microsoft.EntityFrameworkCore;
using Scve.Domain.Entities;

namespace Scve.Infrastructure.Persistence;

public class ScveDbContext(DbContextOptions<ScveDbContext> options) : DbContext(options)
{
    public DbSet<Product> Products => Set<Product>();
    public DbSet<Sale> Sales => Set<Sale>();
    public DbSet<SaleItem> SaleItems => Set<SaleItem>();

    protected override void OnModelCreating(ModelBuilder modelBuilder) =>
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(ScveDbContext).Assembly);
}
