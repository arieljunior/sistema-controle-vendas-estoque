using Microsoft.EntityFrameworkCore;
using Scve.Domain.Entities;
using Scve.Domain.Interfaces;
using Scve.Infrastructure.Persistence;

namespace Scve.Infrastructure.Repositories;

public class EfProductRepository(ScveDbContext db) : IProductRepository
{
    public async Task<IReadOnlyList<Product>> ListAsync() =>
        await db.Products.AsNoTracking().OrderBy(p => p.FlavorName).ToListAsync();

    public Task<Product?> GetByIdAsync(Guid id) =>
        db.Products.FirstOrDefaultAsync(p => p.Id == id);
}
