package org.example.scve.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Relatório de fechamento do dia (vendas concluídas entre 00:00:00 e 23:59:59)")
public record DailyReportResponse(
        @Schema(example = "2026-11-21") LocalDate date,
        @Schema(description = "Total faturado em dinheiro", example = "120.00") BigDecimal totalCash,
        @Schema(description = "Total faturado em Pix", example = "85.50") BigDecimal totalPix,
        @Schema(description = "Faturamento total (dinheiro + Pix)", example = "205.50") BigDecimal totalRevenue,
        @Schema(description = "Total de itens vendidos", example = "21") int totalItemsSold,
        @Schema(description = "Ranking em ordem decrescente de quantidade vendida") List<TopProductResponse> topProducts
) {
}
