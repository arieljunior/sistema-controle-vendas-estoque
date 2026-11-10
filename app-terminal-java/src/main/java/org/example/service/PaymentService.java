package org.example.service;

import org.example.model.Money;
import org.example.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class PaymentService {
    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    public record PaymentResult(BigDecimal amountPaid, BigDecimal change) {
    }

    public PaymentResult process(PaymentMethod method, BigDecimal total, BigDecimal amountPaid) {
        return switch (method) {
            case CASH -> payWithCash(total, amountPaid);
            case PIX -> payWithPix(total);
        };
    }

    private PaymentResult payWithCash(BigDecimal total, BigDecimal amountPaid) {
        if (amountPaid == null) {
            throw new IllegalArgumentException("Informe o valor pago em dinheiro.");
        }
        BigDecimal paid = Money.of(amountPaid);
        if (paid.compareTo(total) < 0) {
            throw new IllegalArgumentException("Valor informado é insuficiente para cobrir o total da venda. Faltam: R$ "
                    + total.subtract(paid).toPlainString() + ".");
        }
        return new PaymentResult(paid, paid.subtract(total));
    }

    // MVP: gateway integration is bypassed. Replace this body with the real gateway call when available.
    private PaymentResult payWithPix(BigDecimal total) {
        LOGGER.warning("PIX gateway BYPASS enabled: charge of R$ " + total.toPlainString() + " approved automatically");
        return new PaymentResult(total, null);
    }
}
