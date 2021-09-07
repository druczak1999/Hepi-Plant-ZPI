package com.example.hepiplant.dto;

public class ScheduleDto {

    private int wateringFrequency;
    private int fertilizingFrequency;
    private int mistingFrequency;

    public ScheduleDto() {
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

}
