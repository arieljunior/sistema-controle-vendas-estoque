package org.example.scve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(description = "Item do carrinho enviado no registro da venda")
public record SaleItemRequest(
        @Schema(description = "Identificador do produto", example = "1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01")
        @NotNull(message = "O produto é obrigatório.")
        UUID productId,

        @Schema(description = "Quantidade desejada (maior que zero)", example = "2")
        @NotNull(message = "A quantidade é obrigatória.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        Integer quantity
) {
}
