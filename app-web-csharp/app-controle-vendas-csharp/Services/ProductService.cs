using Scve.Domain.Enums;
using Scve.Services.Models;
using Scve.Domain.Interfaces;

namespace Scve.Services;

public class ProductService(IProductRepository products)
{
    public async Task<IReadOnlyList<ProductListItem>> ListAsync(string? search = null)
    {
        var all = await products.ListAsync();

        IEnumerable<Domain.Entities.Product> filtered = all;
        if (!string.IsNullOrWhiteSpace(search))
        {
            var term = search.Trim();
            filtered = filtered.Where(p =>
                p.FlavorName.Contains(term, StringComparison.OrdinalIgnoreCase) ||
                p.Id.ToString().Contains(term, StringComparison.OrdinalIgnoreCase));
        }

        return filtered
            .Select(p => new ProductListItem(p.Id, p.FlavorName, p.Type.ToLabel(), p.UnitPrice, p.StockQuantity, p.IsLowStock))
            .ToList();
    }
}
