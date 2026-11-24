package org.example.scve.service;

import org.example.scve.dto.DailyReportResponse;
import org.example.scve.dto.ProductResponse;
import org.example.scve.dto.SaleItemResponse;
import org.example.scve.dto.SaleResponse;
import org.example.scve.dto.TopProductResponse;
import org.example.scve.model.PaymentMethod;
import org.example.scve.model.ProductType;
import org.example.scve.model.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

final class SampleData {

    static final UUID CHOCOLATE_ID = UUID.fromString("1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01");
    static final UUID STRAWBERRY_ID = UUID.fromString("2c7a1d2f-4b6e-4d8f-8e22-1b2c3d4e5f02");
    static final UUID VANILLA_ID = UUID.fromString("3d8b2e30-5c7f-4e90-9f33-2c3d4e5f6a03");
    static final UUID SALE_ID = UUID.fromString("9f1e7c52-6a3b-4d0e-8c14-5b2a7d9e3f10");

    private static final int LOW_STOCK_THRESHOLD = 5;

    private SampleData() {
    }

    static ProductResponse product(UUID id, String flavorName, ProductType type, BigDecimal unitPrice, int stockQuantity) {
        return new ProductResponse(id, flavorName, type, unitPrice, stockQuantity, stockQuantity <= LOW_STOCK_THRESHOLD);
    }

    static List<ProductResponse> products() {
        return List.of(
                product(CHOCOLATE_ID, "Chocolate", ProductType.SCOOP, new BigDecimal("12.50"), 10),
                product(STRAWBERRY_ID, "Morango", ProductType.POPSICLE, new BigDecimal("5.00"), 10),
                product(VANILLA_ID, "Baunilha", ProductType.SCOOP, new BigDecimal("10.00"), 5)
        );
    }

    static SaleResponse sale(UUID id, PaymentMethod method) {
        List<SaleItemResponse> items = List.of(
                new SaleItemResponse(CHOCOLATE_ID, "Chocolate", 2, new BigDecimal("12.50"), new BigDecimal("25.00")),
                new SaleItemResponse(STRAWBERRY_ID, "Morango", 1, new BigDecimal("5.00"), new BigDecimal("5.00"))
        );
        BigDecimal total = new BigDecimal("30.00");
        boolean cash = method == PaymentMethod.CASH;
        return new SaleResponse(
                id,
                LocalDateTime.of(2026, 11, 21, 10, 30),
                method,
                total,
                cash ? new BigDecimal("50.00") : total,
                cash ? new BigDecimal("20.00") : new BigDecimal("0.00"),
                SaleStatus.COMPLETED,
                items
        );
    }

    static DailyReportResponse dailyReport(LocalDate date) {
        return new DailyReportResponse(
                date,
                new BigDecimal("120.00"),
                new BigDecimal("85.50"),
                new BigDecimal("205.50"),
                21,
                List.of(
                        new TopProductResponse(CHOCOLATE_ID, "Chocolate", 10),
                        new TopProductResponse(STRAWBERRY_ID, "Morango", 8),
                        new TopProductResponse(VANILLA_ID, "Baunilha", 3)
                )
        );
    }
}
