package org.example.scve.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductType {
    POPSICLE("PICOLE"),
    SCOOP("MASSA");

    private final String code;

    ProductType(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static ProductType fromCode(String code) {
        for (ProductType value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Tipo de produto inválido: " + code + ". Valores aceitos: PICOLE, MASSA.");
    }
}
