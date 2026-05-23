package com.smarttours.atlasguidebackend.domain.user.output;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single event within a day's itinerary,
 * such as an attraction, meal, or activity.
 */
public class Event {

    @JsonProperty(value = "type", required = true)
    private String type;

    @JsonProperty(value = "start_time", required = true)
    private String startTime;

    @JsonProperty(value = "end_time", required = true)
    private String endTime;

    @JsonProperty(value = "fiche_de_visite", required = true)
    private FicheDeVisite ficheDeVisite;

    // Constructors
    public Event() {}

    public Event(String type, String startTime, String endTime, FicheDeVisite ficheDeVisite) {
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ficheDeVisite = ficheDeVisite;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public FicheDeVisite getFicheDeVisite() {
        return ficheDeVisite;
    }

    public void setFicheDeVisite(FicheDeVisite ficheDeVisite) {
        this.ficheDeVisite = ficheDeVisite;
    }

    @Override
    public String toString() {
        return "Event{" +
                "type='" + type + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", ficheDeVisite=" + ficheDeVisite +
                '}';
    }
}