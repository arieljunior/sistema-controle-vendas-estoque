package org.example.scve.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.example.scve.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Venda a ser registrada. O carrinho é enviado de uma só vez em 'items'")
public record SaleRequest(
        @Schema(description = "Itens do carrinho (ao menos um)")
        @NotEmpty(message = "O carrinho está vazio. Adicione ao menos um item.")
        List<@Valid SaleItemRequest> items,

        @Schema(description = "Forma de pagamento", example = "DINHEIRO", allowableValues = {"DINHEIRO", "PIX"})
        @NotNull(message = "A forma de pagamento é obrigatória.")
        PaymentMethod paymentMethod,

        @Schema(description = "Valor entregue pelo cliente. Obrigatório quando a forma de pagamento é DINHEIRO (RN04)",
                example = "30.00")
        @PositiveOrZero(message = "O valor pago não pode ser negativo.")
        BigDecimal amountPaid
) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "Informe o valor pago para pagamentos em dinheiro.")
    public boolean isAmountPaidInformedForCash() {
        return paymentMethod != PaymentMethod.CASH || amountPaid != null;
    }
}
