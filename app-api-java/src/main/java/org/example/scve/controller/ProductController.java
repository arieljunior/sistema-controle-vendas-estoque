package org.example.scve.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.scve.dto.ProductRequest;
import org.example.scve.dto.ProductResponse;
import org.example.scve.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produtos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Consultar estoque",
            description = "Lista todos os produtos com preço, quantidade e alerta de estoque baixo.")
    @ApiResponse(responseCode = "200", description = "Lista de produtos")
    public List<ProductResponse> list() {
        return productService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por id")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    public ProductResponse getById(@Parameter(description = "Id do produto") @PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Cadastrar produto",
            description = "Cadastra um novo sabor/tipo no catálogo.")
    @ApiResponse(responseCode = "201", description = "Produto cadastrado")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "422", ref = "#/components/responses/UnprocessableEntity")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto",
            description = "Atualiza nome, tipo, preço e estoque de um produto existente.")
    @ApiResponse(responseCode = "200", description = "Produto atualizado")
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    public ProductResponse update(@Parameter(description = "Id do produto") @PathVariable UUID id,
                                  @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover produto")
    @ApiResponse(responseCode = "204", description = "Produto removido")
    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    public ResponseEntity<Void> delete(@Parameter(description = "Id do produto") @PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
