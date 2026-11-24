package org.example.scve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.scve.model.PaymentMethod;
import org.example.scve.model.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Venda registrada (recibo)")
public record SaleResponse(
        @Schema(example = "9f1e7c52-6a3b-4d0e-8c14-5b2a7d9e3f10") UUID id,
        @Schema(example = "2026-11-21T10:30:00") LocalDateTime dateTime,
        @Schema(example = "DINHEIRO", allowableValues = {"DINHEIRO", "PIX"}) PaymentMethod paymentMethod,
        @Schema(example = "30.00") BigDecimal totalAmount,
        @Schema(example = "50.00") BigDecimal amountPaid,
        @Schema(description = "Troco = valor pago - valor total (RN05). Zero no Pix", example = "20.00")
        BigDecimal change,
        @Schema(example = "CONCLUIDA", allowableValues = {"PENDENTE", "CONCLUIDA", "CANCELADA"}) SaleStatus status,
        List<SaleItemResponse> items
) {
}
