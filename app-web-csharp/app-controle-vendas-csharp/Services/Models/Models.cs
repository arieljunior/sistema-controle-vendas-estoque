namespace Scve.Services.Models;

public record ProductListItem(Guid Id, string Name, string TypeLabel, decimal UnitPrice, int Stock, bool IsLowStock);

public record LineView(string Name, int Quantity, decimal UnitPrice, decimal Subtotal);

public record CartView(IReadOnlyList<LineView> Lines, decimal Total)
{
    public bool IsEmpty => Lines.Count == 0;
}

public record ReceiptView(
    Guid Id,
    DateTime DateTime,
    string PaymentLabel,
    string StatusLabel,
    IReadOnlyList<LineView> Lines,
    decimal Total,
    decimal AmountPaid,
    decimal? Change);
