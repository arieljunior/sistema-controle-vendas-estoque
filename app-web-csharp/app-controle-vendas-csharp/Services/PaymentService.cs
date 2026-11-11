using Scve.Domain.Enums;
using Scve.Domain.Exceptions;

namespace Scve.Services;

public record PaymentResult(decimal AmountPaid, decimal? Change);

public class PaymentService(ILogger<PaymentService> logger)
{
    public PaymentResult Process(PaymentMethod method, decimal total, decimal? amountPaid) => method switch
    {
        PaymentMethod.Cash => PayWithCash(total, amountPaid),
        PaymentMethod.Pix => PayWithPix(total),
        _ => throw new BusinessException("Forma de pagamento inválida.")
    };

    private static PaymentResult PayWithCash(decimal total, decimal? amountPaid)
    {
        if (amountPaid is null)
            throw new BusinessException("Informe o valor pago em dinheiro.");

        var paid = Math.Round(amountPaid.Value, 2, MidpointRounding.AwayFromZero);
        if (paid < total)
            throw new BusinessException(
                $"Valor informado é insuficiente para cobrir o total da venda. Faltam: {BrazilianMoney.Format(total - paid)}.");

        return new PaymentResult(paid, paid - total);
    }

    // MVP: troque este corpo pela chamada ao gateway Pix real quando existir.
    private PaymentResult PayWithPix(decimal total)
    {
        logger.LogWarning("PIX gateway BYPASS enabled: charge of {Total} approved automatically", total);
        return new PaymentResult(total, null);
    }
}
