using Scve.Domain.Exceptions;

namespace Scve.Domain.Entities;

public class CartItem(Product product, int quantity)
{
    public Product Product { get; } = product;
    public int Quantity { get; private set; } = quantity;
    public decimal Subtotal => Product.UnitPrice * Quantity;

    public void IncreaseQuantity(int amount) => Quantity += amount;
}

public class Cart
{
    private readonly List<CartItem> _items = [];

    public IReadOnlyList<CartItem> Items => _items;
    public bool IsEmpty => _items.Count == 0;
    public decimal Total => _items.Sum(i => i.Subtotal);

    public void AddItem(Product product, int quantity)
    {
        if (quantity <= 0)
            throw new BusinessException("A quantidade deve ser maior que zero.");

        var existing = _items.FirstOrDefault(i => i.Product.Id == product.Id);
        var requested = quantity + (existing?.Quantity ?? 0);
        if (!product.HasStockFor(requested))
            throw new BusinessException(product.InsufficientStockMessage());

        if (existing is null)
            _items.Add(new CartItem(product, quantity));
        else
            existing.IncreaseQuantity(quantity);
    }
}
