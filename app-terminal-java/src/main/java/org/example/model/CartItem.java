package org.example.model;

import java.math.BigDecimal;

public class CartItem {
    private final Product product;
    private int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    void increaseQuantity(int amount) {
        quantity += amount;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal subtotal() {
        return Money.of(product.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
    }
}

