using Scve.Domain.Enums;

namespace Scve.Domain.Entities;

public class SaleItem
{
    public Guid Id { get; private set; } = Guid.NewGuid();
    public Guid ProductId { get; private set; }
    public Product Product { get; private set; } = null!;
    public int Quantity { get; private set; }
    public decimal UnitPriceAtSale { get; private set; }

    public string FlavorName => Product.FlavorName;
    public decimal Subtotal => UnitPriceAtSale * Quantity;

    private SaleItem()
    {
    }

    public SaleItem(Product product, int quantity)
    {
        if (quantity <= 0)
            throw new ArgumentOutOfRangeException(nameof(quantity));

        Product = product;
        ProductId = product.Id;
        Quantity = quantity;
        UnitPriceAtSale = product.UnitPrice;
    }
}

public class Sale
{
    private readonly List<SaleItem> _items = [];

    public Guid Id { get; private set; } = Guid.NewGuid();
    public DateTime DateTime { get; private set; }
    public PaymentMethod PaymentMethod { get; private set; }
    public decimal TotalAmount { get; private set; }
    public decimal AmountPaid { get; private set; }
    public decimal? Change { get; private set; }
    public SaleStatus Status { get; private set; } = SaleStatus.Pending;
    public IReadOnlyList<SaleItem> Items => _items;

    private Sale()
    {
    }

    private Sale(DateTime dateTime, PaymentMethod method, decimal total, IEnumerable<SaleItem> items)
    {
        DateTime = dateTime;
        PaymentMethod = method;
        TotalAmount = total;
        _items.AddRange(items);
    }

    public static Sale FromCart(Cart cart, PaymentMethod method, DateTime now)
    {
        var items = cart.Items.Select(i => new SaleItem(i.Product, i.Quantity));
        return new Sale(now, method, cart.Total, items);
    }

    public void Complete(decimal amountPaid, decimal? change)
    {
        AmountPaid = amountPaid;
        Change = change;
        Status = SaleStatus.Completed;
    }
}
