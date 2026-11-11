using Scve.Domain.Enums;
using Scve.Domain.Exceptions;

namespace Scve.Domain.Entities;

public class Product
{
    public const int LowStockThreshold = 5;
    private const int MaxNameLength = 50;

    public Guid Id { get; }
    public string FlavorName { get; }
    public ProductType Type { get; }
    public decimal UnitPrice { get; }
    public int StockQuantity { get; private set; }

    public Product(Guid id, string flavorName, ProductType type, decimal unitPrice, int stockQuantity)
    {
        if (string.IsNullOrWhiteSpace(flavorName) || flavorName.Trim().Length > MaxNameLength)
            throw new ArgumentException($"Nome/sabor deve ter de 1 a {MaxNameLength} caracteres.", nameof(flavorName));
        if (unitPrice <= 0)
            throw new ArgumentOutOfRangeException(nameof(unitPrice), "O preço deve ser maior que zero.");
        if (stockQuantity < 0)
            throw new ArgumentOutOfRangeException(nameof(stockQuantity), "O estoque não pode ser negativo.");

        Id = id;
        FlavorName = flavorName.Trim();
        Type = type;
        UnitPrice = Math.Round(unitPrice, 2, MidpointRounding.AwayFromZero);
        StockQuantity = stockQuantity;
    }

    public bool IsLowStock => StockQuantity <= LowStockThreshold;

    public bool HasStockFor(int quantity) => quantity <= StockQuantity;

    public void DecreaseStock(int quantity)
    {
        if (quantity <= 0 || !HasStockFor(quantity))
            throw new BusinessException(InsufficientStockMessage());
        StockQuantity -= quantity;
    }

    public void IncreaseStock(int quantity)
    {
        if (quantity <= 0)
            throw new ArgumentOutOfRangeException(nameof(quantity));
        StockQuantity += quantity;
    }

    public string InsufficientStockMessage() =>
        $"Estoque insuficiente para o produto {FlavorName}. Quantidade disponível em estoque: {StockQuantity}.";
}
