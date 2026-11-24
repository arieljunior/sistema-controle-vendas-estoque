package org.example.scve.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SaleStatus {
    PENDING("PENDENTE"),
    COMPLETED("CONCLUIDA"),
    CANCELED("CANCELADA");

    private final String code;

    SaleStatus(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
