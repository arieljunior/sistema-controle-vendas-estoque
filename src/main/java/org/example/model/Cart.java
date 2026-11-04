package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        CartItem existing = findItem(product);
        int requested = quantity + (existing == null ? 0 : existing.getQuantity());
        if (!product.hasStockFor(requested)) {
            throw new IllegalArgumentException("Estoque insuficiente para o produto " + product.getFlavorName()
                    + ". Quantidade disponível em estoque: " + product.getStockQuantity() + ".");
        }
        if (existing == null) {
            items.add(new CartItem(product, quantity));
        } else {
            existing.increaseQuantity(quantity);
        }
    }

    public BigDecimal total() {
        return items.stream().map(CartItem::subtotal).reduce(Money.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private CartItem findItem(Product product) {
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
    }
}
