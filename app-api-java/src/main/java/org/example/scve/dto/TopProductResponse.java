package org.example.scve.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Produto no ranking de mais vendidos do dia")
public record TopProductResponse(
        @Schema(example = "1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01") UUID productId,
        @Schema(example = "Chocolate") String flavorName,
        @Schema(example = "8") int quantitySold
) {
}
