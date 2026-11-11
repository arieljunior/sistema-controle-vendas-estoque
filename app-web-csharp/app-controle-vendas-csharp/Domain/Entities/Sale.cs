using Scve.Domain.Enums;

namespace Scve.Domain.Entities;

public class SaleItem
{
    public Guid ProductId { get; }
    public string FlavorName { get; }
    public int Quantity { get; }
    public decimal UnitPriceAtSale { get; }
    public decimal Subtotal => UnitPriceAtSale * Quantity;

    public SaleItem(Guid productId, string flavorName, int quantity, decimal unitPriceAtSale)
    {
        if (quantity <= 0)
            throw new ArgumentOutOfRangeException(nameof(quantity));

        ProductId = productId;
        FlavorName = flavorName;
        Quantity = quantity;
        UnitPriceAtSale = unitPriceAtSale;
    }
}

public class Sale
{
    public Guid Id { get; } = Guid.NewGuid();
    public DateTime DateTime { get; }
    public PaymentMethod PaymentMethod { get; }
    public decimal TotalAmount { get; }
    public decimal AmountPaid { get; private set; }
    public decimal? Change { get; private set; }
    public SaleStatus Status { get; private set; } = SaleStatus.Pending;
    public IReadOnlyList<SaleItem> Items { get; }

    private Sale(DateTime dateTime, PaymentMethod method, decimal total, IReadOnlyList<SaleItem> items)
    {
        DateTime = dateTime;
        PaymentMethod = method;
        TotalAmount = total;
        Items = items;
    }

    public static Sale FromCart(Cart cart, PaymentMethod method, DateTime now)
    {
        var items = cart.Items
            .Select(i => new SaleItem(i.Product.Id, i.Product.FlavorName, i.Quantity, i.Product.UnitPrice))
            .ToList();
        return new Sale(now, method, cart.Total, items);
    }

    public void Complete(decimal amountPaid, decimal? change)
    {
        AmountPaid = amountPaid;
        Change = change;
        Status = SaleStatus.Completed;
    }
}
