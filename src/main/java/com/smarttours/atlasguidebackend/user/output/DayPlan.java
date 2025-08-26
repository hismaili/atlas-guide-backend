package com.smarttours.atlasguidebackend.user.output;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the itinerary for a single day.
 */
public class DayPlan {

    @JsonProperty("day")
    private int day;

    @JsonProperty("date")
    private String date;

    @JsonProperty("day_title")
    private String dayTitle;

    @JsonProperty("daily_summary")
    private String dailySummary;

    @JsonProperty("events")
    private List<Event> events;

    // Constructors
    public DayPlan() {}

    // Getters and Setters
    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
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

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "DayPlan{" +
                "day=" + day +
                ", date='" + date + '\'' +
                ", dayTitle='" + dayTitle + '\'' +
                ", dailySummary='" + dailySummary + '\'' +
                ", events=" + events +
                '}';
    }
}