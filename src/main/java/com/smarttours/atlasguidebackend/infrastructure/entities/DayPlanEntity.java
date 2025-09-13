package com.smarttours.atlasguidebackend.infrastructure.entities;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

/**
 * Represents the itinerary for a single day.
 */
@Entity
@Table(name = "day_plans")
public class DayPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int dayInPlan;

    private String date;

    private String dayTitle;

    private String dailySummary;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<EventEntity> events;

    // Constructors
    public DayPlanEntity() {}

    // Getters and Setters
    public int getDayInPlan() {
        return dayInPlan;
    }

    public void setDayInPlan(int dayInPlan) {
        this.dayInPlan = dayInPlan;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayTitle() {
        return dayTitle;
    }

    public void setDayTitle(String dayTitle) {
        this.dayTitle = dayTitle;
    }

    public String getDailySummary() {
        return dailySummary;
    }

    public void setDailySummary(String dailySummary) {
        this.dailySummary = dailySummary;
    }

    public List<EventEntity> getEvents() {
        return events;
    }

    public void setEvents(List<EventEntity> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "DayPlan{" +
                "day=" + dayInPlan +
                ", date='" + date + '\'' +
                ", dayTitle='" + dayTitle + '\'' +
                ", dailySummary='" + dailySummary + '\'' +
                ", events=" + events +
                '}';
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}