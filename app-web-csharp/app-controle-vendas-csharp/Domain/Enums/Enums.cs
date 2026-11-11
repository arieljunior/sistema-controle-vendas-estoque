namespace Scve.Domain.Enums;

public enum ProductType
{
    Picole,
    Massa
}

public enum PaymentMethod
{
    Cash,
    Pix
}

public enum SaleStatus
{
    Pending,
    Completed,
    Canceled
}

public static class EnumExtensions
{
    public static string ToLabel(this ProductType type) => type switch
    {
        ProductType.Picole => "Picolé",
        ProductType.Massa => "Massa",
        _ => type.ToString()
    };

    public static ProductType ParseProductType(string label) => label.Trim().ToLowerInvariant() switch
    {
        "picolé" or "picole" => ProductType.Picole,
        "massa" => ProductType.Massa,
        _ => throw new ArgumentException($"Tipo de produto inválido: '{label}'.", nameof(label))
    };

    public static string ToLabel(this PaymentMethod method) => method switch
    {
        PaymentMethod.Cash => "Dinheiro",
        PaymentMethod.Pix => "Pix",
        _ => method.ToString()
    };

    public static string ToLabel(this SaleStatus status) => status switch
    {
        SaleStatus.Pending => "Pendente",
        SaleStatus.Completed => "Concluída",
        SaleStatus.Canceled => "Cancelada",
        _ => status.ToString()
    };
}
