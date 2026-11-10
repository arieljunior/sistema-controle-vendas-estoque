package org.example.model;

import java.text.Normalizer;
import java.util.Locale;

public enum ProductType {
    PICOLE("PICOLE", "Picolé"),
    MASSA("MASSA", "Massa");

    private final String code;
    private final String label;

    ProductType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ProductType fromLabel(String text) {
        String normalized = normalize(text);
        for (ProductType type : values()) {
            if (normalize(type.label).equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown product type: " + text);
    }

    private static String normalize(String text) {
        String decomposed = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
    }
}

