package com.smarttours.atlasguidebackend.domain.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smarttours.atlasguidebackend.domain.exceptions.UncompleteItineraryException;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;

import java.util.List;
import java.util.UUID;

public interface ItineraryRepository {

    long saveItineraryRequest(String tripId, ItineraryRequest itineraryRequest) throws JsonProcessingException;

    long saveItineraryResponse(long requestId, ItineraryPlan itineraryPlan) throws JsonProcessingException, UncompleteItineraryException;

    List<ItineraryPlan> getItinerariesByUserId(UUID userId);

    List<ItineraryPlan> getAllItineraries();

    ItineraryPlan getItineraryByTripId(String tripId);
}
