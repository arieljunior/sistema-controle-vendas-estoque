using Scve.Domain.Entities;
using Scve.Domain.Exceptions;
using Scve.Domain.Interfaces;
using Scve.Services.Models;

namespace Scve.Services;

public class CartService(ICartStore store, IProductRepository products)
{
    public async Task<CartView> GetViewAsync()
    {
        var cart = await LoadAsync();
        var lines = cart.Items
            .Select(i => new LineView(i.Product.FlavorName, i.Quantity, i.Product.UnitPrice, i.Subtotal))
            .ToList();
        return new CartView(lines, cart.Total);
    }

    public async Task AddItemAsync(Guid productId, int quantity)
    {
        var product = await products.GetByIdAsync(productId)
            ?? throw new BusinessException("Produto não encontrado. Verifique o código digitado.");

        var cart = await LoadAsync();
        cart.AddItem(product, quantity);
        await store.SaveAsync(cart.Items.Select(i => new CartEntry(i.Product.Id, i.Quantity)).ToList());
    }

    public Task ClearAsync() => store.ClearAsync();

    internal async Task<Cart> LoadAsync()
    {
        var cart = new Cart();
        foreach (var entry in await store.GetAsync())
        {
            var product = await products.GetByIdAsync(entry.ProductId);
            if (product is not null)
                cart.AddItem(product, entry.Quantity);
        }
        return cart;
    }
}
