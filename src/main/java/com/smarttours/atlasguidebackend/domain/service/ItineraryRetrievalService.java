package com.smarttours.atlasguidebackend.domain.service;

import com.smarttours.atlasguidebackend.domain.repositories.ItineraryRepository;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItineraryRetrievalService {

    private final ItineraryRepository itineraryRepository;

    public ItineraryRetrievalService(ItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    public ItineraryPlan retrieveItineraryByTripId(String tripId) {
        return itineraryRepository.getItineraryByTripId(tripId);
    }

    public List<ItineraryPlan> retrieveAllItineraries() {
        return itineraryRepository.getAllItineraries();
    }

    public List<ItineraryPlan> retrieveItinerariesByOwnerName(String ownerName) {
        return itineraryRepository.getItinerariesByOwnerName(ownerName);
    }

    public boolean deleteByTripId(String tripId) {
        return itineraryRepository.deleteByTripId(tripId);
    }

    public ItineraryPlan updateTripTitle(String tripId, String title) {
        return itineraryRepository.updateTripTitle(tripId, title);
    }
}
