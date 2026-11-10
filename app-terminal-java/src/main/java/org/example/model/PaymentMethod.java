package org.example.model;

public enum PaymentMethod {
    CASH("DINHEIRO", "Dinheiro"),
    PIX("PIX", "Pix");

    private final String code;
    private final String label;

    PaymentMethod(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}

