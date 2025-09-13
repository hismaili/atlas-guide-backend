package com.smarttours.atlasguidebackend.domain.user.output;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single event within a day's itinerary,
 * such as an attraction, meal, or activity.
 */
public class Event {

    @JsonProperty("type")
    private String type;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("fiche_de_visite")
    private FicheDeVisite ficheDeVisite;

    // Constructors
    public Event() {}

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
}