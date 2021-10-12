package com.example.hepiplant.dto;

import java.io.Serializable;

public class EventDto implements Serializable {
    private String eventName;
    private String eventDescription;
    private String eventDate;
    private boolean isDone;
    private Long plantId;

    public Long getPlantId() {
        return plantId;
    }

    public void setPlantId(Long plantId) {
        this.plantId = plantId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}
