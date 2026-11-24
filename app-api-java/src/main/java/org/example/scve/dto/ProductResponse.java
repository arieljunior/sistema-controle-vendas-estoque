package org.example.scve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.scve.model.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Produto do catálogo com sua situação de estoque")
public record ProductResponse(
        @Schema(example = "1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01") UUID id,
        @Schema(example = "Chocolate") String flavorName,
        @Schema(example = "MASSA", allowableValues = {"PICOLE", "MASSA"}) ProductType type,
        @Schema(example = "12.50") BigDecimal unitPrice,
        @Schema(example = "10") int stockQuantity,
        @Schema(description = "Verdadeiro quando o estoque é menor ou igual a 5 unidades (RN06)", example = "false")
        boolean lowStock
) {
}
