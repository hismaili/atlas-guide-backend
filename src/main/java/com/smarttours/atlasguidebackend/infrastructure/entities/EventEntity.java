package com.smarttours.atlasguidebackend.infrastructure.entities;

import jakarta.persistence.*;

/**
 * Represents a single event within a day's itinerary,
 * such as an attraction, meal, or activity.
 */
@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "day_plan_seq")
    @SequenceGenerator(name = "day_plan_seq", sequenceName = "day_plan_seq", allocationSize = 50)

    private Long id;

    private String type;

    private String startTime;

    private String endTime;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_card_id")
    private VisitCardEntity visitCardEntity;

    @ManyToOne
    @JoinColumn(name = "day_plan_id", nullable = false)
    private DayPlanEntity dayPlan;

    public DayPlanEntity getDayPlan() {
        return dayPlan;
    }

    public void setDayPlan(DayPlanEntity dayPlan) {
        this.dayPlan = dayPlan;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}