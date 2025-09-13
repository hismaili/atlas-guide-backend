package com.smarttours.atlasguidebackend.infrastructure.entities;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Represents a single event within a day's itinerary,
 * such as an attraction, meal, or activity.
 */
@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String type;

    private String startTime;

    private String endTime;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private VisitCardEntity visitCardEntity;

    // Constructors
    public EventEntity() {}

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

    public VisitCardEntity getFicheDeVisite() {
        return visitCardEntity;
    }

    public void setFicheDeVisite(VisitCardEntity visitCardEntity) {
        this.visitCardEntity = visitCardEntity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}