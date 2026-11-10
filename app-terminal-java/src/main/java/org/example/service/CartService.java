package org.example.service;

import org.example.model.Cart;

import java.util.UUID;

public class CartService {
    private final ProductService productService;

    public CartService(ProductService productService) {
        this.productService = productService;
    }

    public void addItem(Cart cart, UUID productId, int quantity) {
        cart.addItem(productService.getById(productId), quantity);
    }
}
