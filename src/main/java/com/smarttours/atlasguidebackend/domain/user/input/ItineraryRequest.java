package com.smarttours.atlasguidebackend.domain.user.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Represents the user's complete set of criteria for a new itinerary request.
 * This improved version uses strong types (Enums, LocalDate) and includes
 * validation annotations to ensure data integrity before processing.
 */
public class ItineraryRequest {

    @NotBlank(message = "Destination cannot be blank.")
    private String destination;

    @NotNull(message = "Start date cannot be null.")
    @FutureOrPresent(message = "Start date must be in the present or future.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Trip duration cannot be null.")
    @Min(value = 1, message = "Trip duration must be at least 1 day.")
    private int tripDuration;

    // ---- Optional Fields
    @FutureOrPresent(message = "End date must be in the present or future.")
    private LocalDate endDate;
    private String accommodationLocation;
    private TravelerType travelerType;
    private Budget budget;
    private List<String> interests;
    private List<String> mustSeeList;
    private List<String> avoidList;
    private Pace pace;
    private List<String> transportationPrefs;
    private LocalTime dailyStartTime;
    private String dietaryNeeds;
    private String accessibilityNeeds;
    private String specialOccasion;
    private TourGuidePersona persona;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(int tripDuration) {
        this.tripDuration = tripDuration;
    }

    public String getAccommodationLocation() {
        return accommodationLocation;
    }

    public void setAccommodationLocation(String accommodationLocation) {
        this.accommodationLocation = accommodationLocation;
    }

    public TravelerType getTravelerType() {
        return travelerType;
    }

    public void setTravelerType(TravelerType travelerType) {
        this.travelerType = travelerType;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getMustSeeList() {
        return mustSeeList;
    }

    public void setMustSeeList(List<String> mustSeeList) {
        this.mustSeeList = mustSeeList;
    }

    public List<String> getAvoidList() {
        return avoidList;
    }

    public void setAvoidList(List<String> avoidList) {
        this.avoidList = avoidList;
    }

    public Pace getPace() {
        return pace;
    }

    public void setPace(Pace pace) {
        this.pace = pace;
    }

    public List<String> getTransportationPrefs() {
        return transportationPrefs;
    }

    public void setTransportationPrefs(List<String> transportationPrefs) {
        this.transportationPrefs = transportationPrefs;
    }

    public LocalTime getDailyStartTime() {
        return dailyStartTime;
    }

    public void setDailyStartTime(LocalTime dailyStartTime) {
        this.dailyStartTime = dailyStartTime;
    }

    public String getDietaryNeeds() {
        return dietaryNeeds;
    }

    public void setDietaryNeeds(String dietaryNeeds) {
        this.dietaryNeeds = dietaryNeeds;
    }

    public String getAccessibilityNeeds() {
        return accessibilityNeeds;
    }

    public void setAccessibilityNeeds(String accessibilityNeeds) {
        this.accessibilityNeeds = accessibilityNeeds;
    }

    public String getSpecialOccasion() {
        return specialOccasion;
    }

    public void setSpecialOccasion(String specialOccasion) {
        this.specialOccasion = specialOccasion;
    }

    public TourGuidePersona getPersona() {
        return persona;
    }

    public void setPersona(TourGuidePersona persona) {
        this.persona = persona;
    }

    @Override
    public String toString() {
        return "UserItineraryRequest{" +
                "destination='" + destination + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", tripDuration=" + tripDuration +
                '}';
    }
}