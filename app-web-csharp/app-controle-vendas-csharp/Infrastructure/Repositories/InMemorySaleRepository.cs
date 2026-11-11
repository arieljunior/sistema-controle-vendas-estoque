using Scve.Domain.Entities;
using Scve.Domain.Interfaces;

namespace Scve.Infrastructure.Repositories;

public class InMemorySaleRepository : ISaleRepository
{
    private readonly List<Sale> _sales = [];
    private readonly Lock _lock = new();

    public Task AddAsync(Sale sale)
    {
        lock (_lock) _sales.Add(sale);
        return Task.CompletedTask;
    }

    public Task<Sale?> GetByIdAsync(Guid id)
    {
        lock (_lock) return Task.FromResult(_sales.FirstOrDefault(s => s.Id == id));
    }
}
