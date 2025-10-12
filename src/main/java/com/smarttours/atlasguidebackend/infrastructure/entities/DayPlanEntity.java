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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private int dayInPlan;

    private String date;

    private String dayTitle;

    @Lob
    private String dailySummary;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final DayPlanEntity dayPlan = new DayPlanEntity();

        public DayPlanEntity build() {
            return dayPlan;
        }

        public DayPlanEntity.Builder withDayNumber(int dayNumber) {
            dayPlan.setDayInPlan(dayNumber);
            return this;
        }

        public DayPlanEntity.Builder withDate(String date) {
            dayPlan.setDate(date);
            return this;
        }

        public DayPlanEntity.Builder withDayTitle(String dayTitle) {
            dayPlan.setDayTitle(dayTitle);
            return this;
        }

        public DayPlanEntity.Builder withDailySummary(String dailySummary) {
            dayPlan.setDailySummary(dailySummary);
            return this;
        }

        public DayPlanEntity.Builder withEvents(List<EventEntity> events) {
            dayPlan.setEvents(events);
            return this;
        }


        public Builder withId(Long uuid) {
            dayPlan.setId(uuid);
            return this;
        }
    }
}