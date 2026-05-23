package com.smarttours.atlasguidebackend.domain.user.output;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * A detailed information card ("fiche de visite") for a specific attraction,
 * restaurant, or point of interest.
 */
public class FicheDeVisite {

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "category", required = true)
    private String category;

    @JsonProperty(value = "description", required = true)
    private String description;

    @JsonProperty(value = "address", required = true)
    private String address;

    @JsonProperty(value = "opening_hours", required = true)
    private String openingHours;

    @JsonProperty(value = "estimated_duration_minutes", required = true)
    private int estimatedDurationMinutes;

    @JsonProperty(value = "ticket_info", required = true)
    private String ticketInfo;

    @JsonProperty(value = "why_its_for_you", required = true)
    private String whyItsForYou;

    @JsonProperty(value = "insider_tip", required = true)
    private String insiderTip;

    @JsonProperty(value = "logistics", required = true)
    private String logistics;


    private String securityDescription;

    private String[] nearByTransportations;

    private String yourResponseAccuracy;

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

    public String getSecurityDescription() {
        return securityDescription;
    }

    public void setSecurityDescription(String securityDescription) {
        this.securityDescription = securityDescription;
    }

    public String[] getNearByTransportations() {
        return nearByTransportations;
    }

    public void setNearByTransportations(String[] nearByTransportations) {
        this.nearByTransportations = nearByTransportations;
    }

    public String getYourResponseAccuracy() {
        return yourResponseAccuracy;
    }

    public void setYourResponseAccuracy(String yourResponseAccuracy) {
        this.yourResponseAccuracy = yourResponseAccuracy;
    }

    @Override
    public String toString() {
        return "FicheDeVisite{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", openingHours='" + openingHours + '\'' +
                ", estimatedDurationMinutes=" + estimatedDurationMinutes +
                ", ticketInfo='" + ticketInfo + '\'' +
                ", whyItsForYou='" + whyItsForYou + '\'' +
                ", insiderTip='" + insiderTip + '\'' +
                ", logistics='" + logistics + '\'' +
                ", securityDescription='" + securityDescription + '\'' +
                ", nearByTransportations=" + Arrays.toString(nearByTransportations) +
                ", yourResponseAccuracy='" + yourResponseAccuracy + '\'' +
                '}';
    }
}