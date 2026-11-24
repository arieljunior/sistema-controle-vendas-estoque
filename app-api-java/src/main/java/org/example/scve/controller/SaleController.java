package org.example.scve.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.scve.dto.SaleRequest;
import org.example.scve.dto.SaleResponse;
import org.example.scve.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Vendas")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @Operation(summary = "Registrar venda",
            description = """
                    Registra a venda de um carrinho. Valida estoque, processa o pagamento \
                    em Dinheiro ou Pix e dá baixa no estoque. \
                    Retorna o recibo da venda.""")
    @ApiResponse(responseCode = "201", description = "Venda concluída")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    @ApiResponse(responseCode = "422", ref = "#/components/responses/UnprocessableEntity")
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest request) {
        SaleResponse sale = saleService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(sale.id()).toUri();
        return ResponseEntity.created(location).body(sale);
    }

    @GetMapping
    @Operation(summary = "Listar vendas")
    @ApiResponse(responseCode = "200", description = "Lista de vendas")
    public List<SaleResponse> list() {
        return saleService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar venda (recibo)")
    @ApiResponse(responseCode = "200", description = "Venda encontrada")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    public SaleResponse getById(@Parameter(description = "Id da venda") @PathVariable UUID id) {
        return saleService.getById(id);
    }
}
