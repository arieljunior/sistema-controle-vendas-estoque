package org.example.model;

public enum SaleStatus {
    PENDING("PENDENTE"),
    COMPLETED("CONCLUIDA"),
    CANCELED("CANCELADA");

    private final String code;

    SaleStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

