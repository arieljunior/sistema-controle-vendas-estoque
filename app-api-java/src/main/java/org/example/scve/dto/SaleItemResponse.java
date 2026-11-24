package org.example.scve.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Item de uma venda, com o preço praticado no momento da venda")
public record SaleItemResponse(
        @Schema(example = "1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01") UUID productId,
        @Schema(example = "Chocolate") String flavorName,
        @Schema(example = "2") int quantity,
        @Schema(example = "12.50") BigDecimal unitPriceAtSale,
        @Schema(example = "25.00") BigDecimal subtotal
) {
}
