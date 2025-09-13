package com.smarttours.atlasguidebackend.infrastructure.repository;

import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItineraryPlanRepositoryTest {

    @Autowired
    private ItineraryPlanRepository itineraryPlanRepository;

    @DisplayName("Should save and retrieve an ItineraryPlanEntity successfully")
    @Test
    void saveAndRetrieveItineraryPlanEntity() {

        ItineraryPlanEntity entity = new ItineraryPlanEntity();
        entity.setTripSummary("Sample Itinerary");

        ItineraryPlanEntity savedEntity = itineraryPlanRepository.save(entity);
        UUID id = savedEntity.getId();
        assertThat(id).isNotNull();

        Optional<ItineraryPlanEntity> retrievedEntity = itineraryPlanRepository.findById(id);

        assertThat(retrievedEntity).isPresent();
        assertThat(retrievedEntity.get().getTripSummary()).isEqualTo("Sample Itinerary");
    }

    @DisplayName("Should return empty when retrieving a non-existent ItineraryPlanEntity")
    @Test
    void retrieveNonExistentItineraryPlanEntity() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<ItineraryPlanEntity> retrievedEntity = itineraryPlanRepository.findById(nonExistentId);

        assertThat(retrievedEntity).isEmpty();
    }

    @DisplayName("Should delete an ItineraryPlanEntity successfully")
    @Test
    void deleteItineraryPlanEntity() {

        ItineraryPlanEntity entity = new ItineraryPlanEntity();
        entity.setTripSummary("Itinerary to Delete");

        ItineraryPlanEntity savedEntity = itineraryPlanRepository.save(entity);
        assertThat(savedEntity).isNotNull();
        UUID id = savedEntity.getId();
        assertThat(id).isNotNull();
        itineraryPlanRepository.deleteById(id);

        Optional<ItineraryPlanEntity> retrievedEntity = itineraryPlanRepository.findById(id);
        assertThat(retrievedEntity).isEmpty();
    }
}