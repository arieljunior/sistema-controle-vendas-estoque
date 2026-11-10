package org.example.model;

import java.math.BigDecimal;
import java.util.UUID;

public class SaleItem {
    private final UUID id = UUID.randomUUID();
    private final UUID saleId;
    private final UUID productId;
    private final int quantity;
    private final BigDecimal unitPriceAtSale;

    public SaleItem(UUID saleId, UUID productId, int quantity, BigDecimal unitPriceAtSale) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPriceAtSale = Money.of(unitPriceAtSale);
    }

    public BigDecimal subtotal() {
        return Money.of(unitPriceAtSale.multiply(BigDecimal.valueOf(quantity)));
    }

    public UUID getId() {
        return id;
    }

    public UUID getSaleId() {
        return saleId;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPriceAtSale() {
        return unitPriceAtSale;
    }
}

