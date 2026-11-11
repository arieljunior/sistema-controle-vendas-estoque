using System.Globalization;
using System.Text.RegularExpressions;

namespace Scve.Services;

public static partial class BrazilianMoney
{
    private static readonly CultureInfo PtBr = CultureInfo.GetCultureInfo("pt-BR");

    // Aceita 50, 50,5, 50,00, 1.250,50 e R$ 1.250,50; rejeita 50.00.
    [GeneratedRegex(@"^(\d{1,3}(\.\d{3})+|\d+)(,\d{1,2})?$")]
    private static partial Regex ValidFormat();

    public static bool TryParse(string? input, out decimal value)
    {
        value = 0;
        if (string.IsNullOrWhiteSpace(input))
            return false;

        var text = input.Trim();
        if (text.StartsWith("R$", StringComparison.OrdinalIgnoreCase))
            text = text[2..];
        text = text.Trim();

        if (!ValidFormat().IsMatch(text))
            return false;

        var normalized = text.Replace(".", "").Replace(',', '.');
        if (!decimal.TryParse(normalized, NumberStyles.AllowDecimalPoint, CultureInfo.InvariantCulture, out value))
            return false;

        return value > 0;
    }

    public static string Format(decimal value) => value.ToString("C", PtBr);
}
