using Scve.Domain.Entities;

namespace Scve.Domain.Interfaces;

public record CartEntry(Guid ProductId, int Quantity);

public interface IProductRepository
{
    Task<IReadOnlyList<Product>> ListAsync();
    Task<Product?> GetByIdAsync(Guid id);
    Task SaveAllAsync();
}

public interface ISaleRepository
{
    Task AddAsync(Sale sale);
    Task<Sale?> GetByIdAsync(Guid id);
}

public interface ICartStore
{
    Task<IReadOnlyList<CartEntry>> GetAsync();
    Task SaveAsync(IReadOnlyList<CartEntry> entries);
    Task ClearAsync();
}
