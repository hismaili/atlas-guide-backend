package com.smarttours.atlasguidebackend.infrastructure.repository.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smarttours.atlasguidebackend.domain.exceptions.UncompleteItineraryException;
import com.smarttours.atlasguidebackend.domain.repositories.ItineraryRepository;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.ItineraryPlanJpaRepository;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.UserItineraryRequestJpaRepository;
import com.smarttours.atlasguidebackend.infrastructure.repository.utils.UserItineraryRequestMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ConcreteItineraryRepository implements ItineraryRepository {

    private final UserItineraryRequestJpaRepository userItineraryRequestJpaRepository;

    private final ItineraryPlanJpaRepository itineraryPlanJpaRepository;

    public ConcreteItineraryRepository(UserItineraryRequestJpaRepository userItineraryRequestJpaRepository, ItineraryPlanJpaRepository itineraryPlanJpaRepository) {
        this.userItineraryRequestJpaRepository = userItineraryRequestJpaRepository;
        this.itineraryPlanJpaRepository = itineraryPlanJpaRepository;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public long saveItineraryRequest(String tripId, ItineraryRequest itineraryRequest) throws JsonProcessingException {
        UserItineraryRequest userItineraryRequest = UserItineraryRequestMapper.toEntity(itineraryRequest, tripId);
        UserItineraryRequest savedItineraryRequest = userItineraryRequestJpaRepository.save(userItineraryRequest);

        return savedItineraryRequest.getUserItineraryRequestId();
    }
    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public long saveItineraryResponse(long requestId, ItineraryPlan itineraryPlan) throws JsonProcessingException, UncompleteItineraryException {
        UserItineraryRequest userItineraryRequest = userItineraryRequestJpaRepository.getReferenceById(requestId);

        // Update the itinerary response JSON
        userItineraryRequest.setItineraryResponseJson(UserItineraryRequestMapper.itineraryPlanToJson(itineraryPlan));
        UserItineraryRequest savedItineraryRequest = userItineraryRequestJpaRepository.save(userItineraryRequest);

        // Save the ItineraryPlan entity
        itineraryPlanJpaRepository.save(UserItineraryRequestMapper.itineraryPlanToEntity(itineraryPlan));

        return savedItineraryRequest.getUserItineraryRequestId();
    }
}
