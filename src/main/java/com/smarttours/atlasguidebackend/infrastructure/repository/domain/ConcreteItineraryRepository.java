package com.smarttours.atlasguidebackend.infrastructure.repository.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.exceptions.UncompleteItineraryException;
import com.smarttours.atlasguidebackend.domain.repositories.ItineraryRepository;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.ItineraryPlanJpaRepository;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.UserItineraryRequestJpaRepository;
import com.smarttours.atlasguidebackend.infrastructure.repository.utils.ItineraryPlanMapper;
import com.smarttours.atlasguidebackend.infrastructure.repository.utils.UserItineraryRequestMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ConcreteItineraryRepository implements ItineraryRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ConcreteItineraryRepository.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UserItineraryRequestJpaRepository userItineraryRequestJpaRepository;
    private final ItineraryPlanJpaRepository itineraryPlanJpaRepository;

    public ConcreteItineraryRepository(UserItineraryRequestJpaRepository userItineraryRequestJpaRepository, ItineraryPlanJpaRepository itineraryPlanJpaRepository) {
        this.userItineraryRequestJpaRepository = userItineraryRequestJpaRepository;
        this.itineraryPlanJpaRepository = itineraryPlanJpaRepository;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public long saveItineraryRequest(Map<String, Object> userDetails, String tripId, ItineraryRequest itineraryRequest) throws JsonProcessingException {
        UserItineraryRequest userItineraryRequest = UserItineraryRequestMapper.toEntity(userDetails, itineraryRequest, tripId);
        UserItineraryRequest savedItineraryRequest = userItineraryRequestJpaRepository.save(userItineraryRequest);
        return savedItineraryRequest.getUserItineraryRequestId();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public long saveItineraryResponse(long requestId, ItineraryPlan itineraryPlan) throws JsonProcessingException, UncompleteItineraryException {
        UserItineraryRequest userItineraryRequest = userItineraryRequestJpaRepository.getReferenceById(requestId);

        // Update the itinerary response JSON
        userItineraryRequest.setItineraryResponseJson(ItineraryPlanMapper.itineraryPlanToJson(itineraryPlan));
        UserItineraryRequest savedItineraryRequest = userItineraryRequestJpaRepository.save(userItineraryRequest);

        // Extract context fields from the original request JSON
        String destination = "";
        String startDate = "";
        int tripDuration = 0;
        String requestJson = userItineraryRequest.getItineraryRequestJson();
        if (requestJson != null) {
            try {
                JsonNode node = objectMapper.readTree(requestJson);
                destination = node.path("destination").asText("");
                startDate = node.path("startDate").asText("");
                tripDuration = node.path("tripDuration").asInt(0);
            } catch (JsonProcessingException e) {
                LOG.warn("Could not parse itineraryRequestJson for context fields", e);
            }
        }

        // Save the ItineraryPlan entity with context fields
        ItineraryPlanEntity entity = ItineraryPlanMapper.itineraryPlanToEntity(
                itineraryPlan, userItineraryRequest.getTripId(), destination, startDate, tripDuration);
        ItineraryPlanEntity savedEntity = itineraryPlanJpaRepository.save(entity);

        // Link the plan entity to the request for the owner-based query
        savedItineraryRequest.setItineraryPlanEntity(savedEntity);
        userItineraryRequestJpaRepository.save(savedItineraryRequest);

        return savedItineraryRequest.getUserItineraryRequestId();
    }

    @Override
    public List<ItineraryPlan> getItinerariesByUserId(UUID userId) {
        return null;
    }

    @Override
    @Transactional
    public List<ItineraryPlan> getAllItineraries() {
        List<ItineraryPlanEntity> itineraryPlans = itineraryPlanJpaRepository.findAll();
        return itineraryPlans.stream()
                .map(ItineraryPlanMapper::itineraryPlanEntityToDomain)
                .toList();
    }

    @Override
    @Transactional
    public ItineraryPlan getItineraryByTripId(String tripId) {
        ItineraryPlanEntity entity = itineraryPlanJpaRepository.findByTripId(tripId);
        return ItineraryPlanMapper.itineraryPlanEntityToDomain(entity);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean deleteByTripId(String tripId) {
        ItineraryPlanEntity entity = itineraryPlanJpaRepository.findByTripId(tripId);
        if (entity == null) {
            return false;
        }
        itineraryPlanJpaRepository.delete(entity);
        // Also delete the UserItineraryRequest
        UserItineraryRequest request = userItineraryRequestJpaRepository.findByTripId(tripId);
        if (request != null) {
            userItineraryRequestJpaRepository.delete(request);
        }
        return true;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ItineraryPlan updateTripTitle(String tripId, String title) {
        ItineraryPlanEntity entity = itineraryPlanJpaRepository.findByTripId(tripId);
        if (entity == null) {
            return null;
        }
        entity.setTripTitle(title);
        itineraryPlanJpaRepository.save(entity);
        return ItineraryPlanMapper.itineraryPlanEntityToDomain(entity);
    }

    @Override
    @Transactional
    public List<ItineraryPlan> getItinerariesByOwnerName(String ownerName) {
        List<UserItineraryRequest> requests = userItineraryRequestJpaRepository.findByBlongsToName(ownerName);
        return requests.stream()
                .filter(r -> r.getItineraryPlanEntity() != null)
                .map(r -> ItineraryPlanMapper.itineraryPlanEntityToDomain(r.getItineraryPlanEntity()))
                .toList();
    }
}
