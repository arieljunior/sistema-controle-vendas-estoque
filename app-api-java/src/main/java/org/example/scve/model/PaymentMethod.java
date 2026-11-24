package org.example.scve.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    CASH("DINHEIRO"),
    PIX("PIX");

    private final String code;

    PaymentMethod(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Forma de pagamento inválida: " + code + ". Valores aceitos: DINHEIRO, PIX.");
    }
}
