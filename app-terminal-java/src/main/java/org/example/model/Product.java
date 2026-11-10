package org.example.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Product {
    public static final int LOW_STOCK_THRESHOLD = 5;
    private static final int MAX_NAME_LENGTH = 50;

    private final UUID id;
    private final String flavorName;
    private final ProductType type;
    private final BigDecimal unitPrice;
    private int stockQuantity;

    public Product(UUID id, String flavorName, ProductType type, BigDecimal unitPrice, int stockQuantity) {
        if (flavorName == null || flavorName.isBlank() || flavorName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Flavor name must have 1 to " + MAX_NAME_LENGTH + " characters");
        }
        BigDecimal price = Money.of(Objects.requireNonNull(unitPrice));
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity must not be negative");
        }
        this.id = Objects.requireNonNull(id);
        this.flavorName = flavorName.trim();
        this.type = Objects.requireNonNull(type);
        this.unitPrice = price;
        this.stockQuantity = stockQuantity;
    }

    public boolean isLowStock() {
        return stockQuantity <= LOW_STOCK_THRESHOLD;
    }

    public boolean hasStockFor(int quantity) {
        return quantity <= stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0 || !hasStockFor(quantity)) {
            throw new IllegalArgumentException("Invalid quantity to decrease: " + quantity);
        }
        stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity to increase: " + quantity);
        }
        stockQuantity += quantity;
    }

    public UUID getId() {
        return id;
    }

    public String getFlavorName() {
        return flavorName;
    }

    public ProductType getType() {
        return type;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }
}

