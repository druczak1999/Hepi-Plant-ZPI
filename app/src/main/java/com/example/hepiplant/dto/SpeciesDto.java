package com.example.hepiplant.dto;

import com.example.hepiplant.dto.enums.Placement;

public class SpeciesDto {

    private String name;
    private int wateringFrequency;
    private int fertilizingFrequency;
    private int mistingFrequency;
    private Placement placement;
    private String soil;
    private Long categoryId;

    public SpeciesDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWateringFrequency() {
        return wateringFrequency;
    }

    public void setWateringFrequency(int wateringFrequency) {
        this.wateringFrequency = wateringFrequency;
    }

    public int getFertilizingFrequency() {
        return fertilizingFrequency;
    }

    public void setFertilizingFrequency(int fertilizingFrequency) {
        this.fertilizingFrequency = fertilizingFrequency;
    }

    public int getMistingFrequency() {
        return mistingFrequency;
    }

    public void setMistingFrequency(int mistingFrequency) {
        this.mistingFrequency = mistingFrequency;
    }

    public Placement getPlacement() {
        return placement;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public String getSoil() {
        return soil;
    }

    public void setSoil(String soil) {
        this.soil = soil;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
