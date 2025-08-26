package com.smarttours.atlasguidebackend.user.output;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A detailed information card ("fiche de visite") for a specific attraction,
 * restaurant, or point of interest.
 */
public class FicheDeVisite {

    @JsonProperty("name")
    private String name;

    @JsonProperty("category")
    private String category;

    @JsonProperty("description")
    private String description;

    @JsonProperty("address")
    private String address;

    @JsonProperty("opening_hours")
    private String openingHours;

    @JsonProperty("estimated_duration_minutes")
    private int estimatedDurationMinutes;

    @JsonProperty("ticket_info")
    private String ticketInfo;

    @JsonProperty("why_its_for_you")
    private String whyItsForYou;

    @JsonProperty("insider_tip")
    private String insiderTip;

    @JsonProperty("logistics")
    private String logistics;

    // Constructors
    public FicheDeVisite() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public String getTicketInfo() {
        return ticketInfo;
    }

    public void setTicketInfo(String ticketInfo) {
        this.ticketInfo = ticketInfo;
    }

    public String getWhyItsForYou() {
        return whyItsForYou;
    }

    public void setWhyItsForYou(String whyItsForYou) {
        this.whyItsForYou = whyItsForYou;
    }

    public String getInsiderTip() {
        return insiderTip;
    }

    public void setInsiderTip(String insiderTip) {
        this.insiderTip = insiderTip;
    }

    public String getLogistics() {
        return logistics;
    }

    public void setLogistics(String logistics) {
        this.logistics = logistics;
    }
}