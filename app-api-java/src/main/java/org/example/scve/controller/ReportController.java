package org.example.scve.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.scve.dto.DailyReportResponse;
import org.example.scve.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Relatórios")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    @Operation(summary = "Relatório de fechamento do dia",
            description = "Totais por forma de pagamento, faturamento total, itens vendidos e ranking de "
                    + "produtos mais vendidos.")
    @ApiResponse(responseCode = "200", description = "Relatório do dia (zerado quando não há vendas)")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    public DailyReportResponse daily(
            @Parameter(description = "Data do relatório (AAAA-MM-DD). Padrão: hoje", example = "2026-11-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.daily(date);
    }
}
