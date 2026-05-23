package com.smarttours.atlasguidebackend.domain.user.output;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the itinerary for a single day.
 */
public class DayPlan {

    @JsonProperty(value = "day", required = true)
    private int day;

    @JsonProperty(value = "date", required = true)
    private String date;

    @JsonProperty(value = "day_title", required = true)
    private String dayTitle;

    @JsonProperty(value = "daily_summary", required = true)
    private String dailySummary;

    @JsonProperty(value = "events", required = true)
    private List<Event> events;

    // Constructors
    public DayPlan() {}

    public DayPlan(int i, LocalDate localDate, String s, ArrayList<Event> events) {
        this.day = i;
        this.date = localDate.toString();
        this.dayTitle = s;
        this.events = events;
    }

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