package com.smarttours.atlasguidebackend.infrastructure.entities;

import jakarta.persistence.*;

import java.sql.Blob;
import java.util.UUID;

import static jakarta.persistence.GenerationType.UUID;

@Entity
@Table(name = "user_itinerary_requests")
public class UserItineraryRequest {

    @Id
    private UUID userItineraryRequestId;

    private String tripId;

    @Lob
    private String itineraryRequestJson;

    @Lob
    private String itineraryResponseJson;

    @OneToOne(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private ItineraryPlanEntity itineraryPlanEntity;

    // Constructors
    public UserItineraryRequest() {}

    public UserItineraryRequest(String tripId, String itineraryRequestJson, String itineraryResponseJson, ItineraryPlanEntity itineraryPlanEntity) {
        this.tripId = tripId;
        this.itineraryRequestJson = itineraryRequestJson;
        this.itineraryResponseJson = itineraryResponseJson;
        this.itineraryPlanEntity = itineraryPlanEntity;
    }

    // Getters and Setters


    public UUID getUserItineraryRequestId() {
        return userItineraryRequestId;
    }

    public void setUserItineraryRequestId(UUID userItineraryRequestId) {
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

        return getUserItineraryRequestId() != null ? getUserItineraryRequestId().equals(that.getUserItineraryRequestId()) : that.getUserItineraryRequestId() == null;
    }

    @Override
    public int hashCode() {
        return getUserItineraryRequestId() != null ? getUserItineraryRequestId().hashCode() : 0;
    }
}
