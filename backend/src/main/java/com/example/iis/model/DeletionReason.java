package com.example.iis.model;

public enum DeletionReason {
    PLAMENJACA("Plamenjača"),
    PARAZITI("Paraziti"),
    DEHIDRATACIJA("Dehidratacija"),
    BOLEST("Bolest"),
    DRUGO("Drugo");

    private final String label;

    DeletionReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
