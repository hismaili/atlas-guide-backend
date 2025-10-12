package com.smarttours.atlasguidebackend.infrastructure.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * A detailed information card ("fiche de visite") for a specific attraction,
 * restaurant, or point of interest.
 */
@Entity
@Table(name = "visit_cards")
public class VisitCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "day_plan_seq")
    @SequenceGenerator(name = "day_plan_seq", sequenceName = "day_plan_seq", allocationSize = 50)
    private Long id;

    private String name;

    private String category;

    //TODO check the length before saving
    @Column(length = 700)
    private String description;

    private String address;

    private String openingHours;

    private int estimatedDurationMinutes;

    private String ticketInfo;

    @Column(length = 700)
    private String whyItsForYou;

    @Column(length = 700)
    private String insiderTip;

    private String logistics;

    // Constructors
    public VisitCardEntity() {}

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}