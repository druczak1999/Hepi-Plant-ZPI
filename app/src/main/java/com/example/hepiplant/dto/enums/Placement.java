package com.example.hepiplant.dto.enums;

public enum Placement {

    BARDZO_JASNE("Bardzo jasne"),
    JASNE("Jasne"),
    POLCIENISTE("Półcieniste"),
    ZACIENIONE("Zacienione");

    private final String name;

    Placement(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }
}
