using Microsoft.EntityFrameworkCore;
using Scve.Domain.Entities;
using Scve.Domain.Interfaces;
using Scve.Infrastructure.Persistence;

namespace Scve.Infrastructure.Repositories;

public class EfSaleRepository(ScveDbContext db) : ISaleRepository
{
    public async Task AddAsync(Sale sale)
    {
        db.Sales.Add(sale);
        await db.SaveChangesAsync();
    }

    public Task<Sale?> GetByIdAsync(Guid id) =>
        db.Sales
            .AsNoTracking()
            .Include(s => s.Items)
            .ThenInclude(i => i.Product)
            .FirstOrDefaultAsync(s => s.Id == id);
}
