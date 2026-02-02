package com.smarttours.atlasguidebackend.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smarttours.atlasguidebackend.domain.exceptions.ItineraryPersitenceException;
import com.smarttours.atlasguidebackend.domain.exceptions.UncompleteItineraryException;
import com.smarttours.atlasguidebackend.domain.repositories.ItineraryRepository;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserItineraryPersistenceService {

    private final ItineraryRepository itineraryRepository;

    public UserItineraryPersistenceService(ItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    public long saveItineraryRequest(Map<String, Object> userDetails, String tripId, ItineraryRequest request) throws ItineraryPersitenceException {
        try {
            return itineraryRepository.saveItineraryRequest(userDetails, tripId, request);
        } catch (JsonProcessingException e) {
            throw new ItineraryPersitenceException(e);
        }
    }

    public void saveItineraryPlan(long requestId, ItineraryPlan plan) throws ItineraryPersitenceException {
        try {
            itineraryRepository.saveItineraryResponse(requestId, plan);
        } catch (JsonProcessingException | UncompleteItineraryException e) {
            throw new ItineraryPersitenceException(e);
        }
    }
}
