package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Sale {
    private final UUID id;
    private final LocalDateTime dateTime;
    private final PaymentMethod paymentMethod;
    private final BigDecimal totalAmount;
    private final List<SaleItem> items;
    private BigDecimal amountPaid;
    private BigDecimal change;
    private SaleStatus status = SaleStatus.PENDING;

    private Sale(UUID id, PaymentMethod paymentMethod, List<SaleItem> items) {
        this.id = id;
        this.dateTime = LocalDateTime.now();
        this.paymentMethod = paymentMethod;
        this.items = items;
        this.totalAmount = items.stream().map(SaleItem::subtotal).reduce(Money.ZERO, BigDecimal::add);
    }

    public static Sale fromCart(Cart cart, PaymentMethod paymentMethod) {
        UUID saleId = UUID.randomUUID();
        List<SaleItem> items = cart.getItems().stream()
                .map(item -> new SaleItem(saleId, item.getProduct().getId(), item.getQuantity(),
                        item.getProduct().getUnitPrice()))
                .toList();
        return new Sale(saleId, paymentMethod, items);
    }

    public void complete(BigDecimal amountPaid, BigDecimal change) {
        this.amountPaid = Money.of(amountPaid);
        this.change = change == null ? null : Money.of(change);
        this.status = SaleStatus.COMPLETED;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getChange() {
        return change;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public List<SaleItem> getItems() {
        return items;
    }
}

