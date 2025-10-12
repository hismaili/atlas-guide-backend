package com.smarttours.atlasguidebackend.domain.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smarttours.atlasguidebackend.domain.exceptions.UncompleteItineraryException;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;

import java.util.UUID;

public interface ItineraryRepository {

    UUID saveItineraryRequest(String tripId, ItineraryRequest itineraryRequest) throws JsonProcessingException;

    UUID saveItineraryResponse(UUID requestId, ItineraryPlan itineraryPlan) throws JsonProcessingException, UncompleteItineraryException;
}
