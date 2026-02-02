package com.smarttours.atlasguidebackend.infrastructure.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "user_itinerary_requests")
public class UserItineraryRequest {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long userItineraryRequestId;

    private String tripId;

    @Column(columnDefinition = "TEXT")
    private String itineraryRequestJson;

    @Column(columnDefinition = "TEXT")
    private String itineraryResponseJson;

    @OneToOne(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private ItineraryPlanEntity itineraryPlanEntity;

    @ManyToOne(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(name = "blongs_to_id")
    private Owner blongsTo;

    private LocalDateTime createdOn;

    public Owner getBlongsTo() {
        return blongsTo;
    }

    public void setBlongsTo(Owner blongsTo) {
        this.blongsTo = blongsTo;
    }

    // Constructors
    public UserItineraryRequest() {}

    public UserItineraryRequest(String tripId, String itineraryRequestJson, String itineraryResponseJson, ItineraryPlanEntity itineraryPlanEntity) {
        this.tripId = tripId;
        this.itineraryRequestJson = itineraryRequestJson;
        this.itineraryResponseJson = itineraryResponseJson;
        this.itineraryPlanEntity = itineraryPlanEntity;
        this.createdOn = LocalDateTime.now();
    }

    // Getters and Setters


    public Long getUserItineraryRequestId() {
        return userItineraryRequestId;
    }

    public void setUserItineraryRequestId(Long userItineraryRequestId) {
        this.userItineraryRequestId = userItineraryRequestId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getItineraryRequestJson() {
        return itineraryRequestJson;
    }

    public void setItineraryRequestJson(String itineraryRequestJson) {
        this.itineraryRequestJson = itineraryRequestJson;
    }

    public String getItineraryResponseJson() {
        return itineraryResponseJson;
    }

    public void setItineraryResponseJson(String itineraryResponseJson) {
        this.itineraryResponseJson = itineraryResponseJson;
    }

    public ItineraryPlanEntity getItineraryPlanEntity() {
        return itineraryPlanEntity;
    }

    public void setItineraryPlanEntity(ItineraryPlanEntity itineraryPlanEntity) {
        this.itineraryPlanEntity = itineraryPlanEntity;
    }

    // toString()
    @Override
    public String toString() {
        return "UserVisitPlanEntity{" +
                "userVisitRequestId=" + userItineraryRequestId +
                ", tripId='" + tripId + '\'' +
                ", itineraryRequestJson='" + itineraryRequestJson + '\'' +
                ", itineraryResponseJson='" + itineraryResponseJson + '\'' +
                ", itineraryPlanEntity=" + itineraryPlanEntity +
                '}';
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserItineraryRequest that)) return false;

        return getUserItineraryRequestId() == that.getUserItineraryRequestId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUserItineraryRequestId());
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

}
