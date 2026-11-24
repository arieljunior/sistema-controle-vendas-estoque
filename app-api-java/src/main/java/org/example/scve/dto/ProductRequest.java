package org.example.scve.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.example.scve.model.ProductType;

import java.math.BigDecimal;

@Schema(description = "Dados para cadastro ou atualização de um produto")
public record ProductRequest(
        @Schema(description = "Nome/sabor do produto (único no catálogo)", example = "Chocolate", maxLength = 50)
        @NotBlank(message = "O nome/sabor é obrigatório.")
        @Size(max = 50, message = "O nome/sabor deve ter no máximo 50 caracteres.")
        String flavorName,

        @Schema(description = "Tipo do produto", example = "MASSA", allowableValues = {"PICOLE", "MASSA"})
        @NotNull(message = "O tipo é obrigatório.")
        ProductType type,

        @Schema(description = "Preço unitário em R$ (maior que zero)", example = "12.50")
        @NotNull(message = "O preço unitário é obrigatório.")
        @DecimalMin(value = "0.00", inclusive = false, message = "O preço unitário deve ser maior que zero.")
        @DecimalMax(value = "99999999.99", message = "O preço unitário excede o valor máximo permitido.")
        BigDecimal unitPrice,

        @Schema(description = "Quantidade em estoque (maior ou igual a zero)", example = "10")
        @NotNull(message = "A quantidade em estoque é obrigatória.")
        @PositiveOrZero(message = "A quantidade em estoque deve ser maior ou igual a zero.")
        Integer stockQuantity
) {
}
