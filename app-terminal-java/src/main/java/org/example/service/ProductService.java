package org.example.service;

import org.example.model.Product;
import org.example.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    public Product getById(UUID id) {
        return productRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Produto não encontrado. Verifique o código digitado."));
    }
}
