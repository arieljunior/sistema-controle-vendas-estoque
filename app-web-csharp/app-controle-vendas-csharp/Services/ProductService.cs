using Scve.Domain.Enums;
using Scve.Services.Models;
using Scve.Domain.Interfaces;

namespace Scve.Services;

public class ProductService(IProductRepository products)
{
    public async Task<IReadOnlyList<ProductListItem>> ListAsync()
    {
        var all = await products.ListAsync();
        return all
            .Select(p => new ProductListItem(p.Id, p.FlavorName, p.Type.ToLabel(), p.UnitPrice, p.StockQuantity, p.IsLowStock))
            .ToList();
    }
}
