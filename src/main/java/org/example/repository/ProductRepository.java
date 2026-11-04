package org.example.repository;

import org.example.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    List<Product> findAll();

    Optional<Product> findById(UUID id);

    void saveAll(List<Product> products);
}

