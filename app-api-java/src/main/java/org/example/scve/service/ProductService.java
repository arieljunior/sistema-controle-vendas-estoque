package org.example.scve.service;

import org.example.scve.dto.ProductRequest;
import org.example.scve.dto.ProductResponse;
import org.example.scve.model.ProductType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    public List<ProductResponse> list() {
        return SampleData.products();
    }

    public ProductResponse getById(UUID id) {
        return SampleData.product(id, "Chocolate", ProductType.SCOOP, new BigDecimal("12.50"), 10);
    }

    public ProductResponse create(ProductRequest request) {
        return toResponse(UUID.randomUUID(), request);
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        return toResponse(id, request);
    }

    public void delete(UUID id) {
        // nenhuma remoção é realizada.
    }

    private ProductResponse toResponse(UUID id, ProductRequest request) {
        return SampleData.product(id, request.flavorName(), request.type(), request.unitPrice(), request.stockQuantity());
    }
}
