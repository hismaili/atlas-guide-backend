package com.smarttours.atlasguidebackend.infrastructure.repository;

import com.smarttours.atlasguidebackend.infrastructure.entities.DayPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.ItineraryPlanJpaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItineraryPlanRepositoryTest {

    @Autowired
    private ItineraryPlanJpaRepository itineraryPlanRepository;

    @DisplayName("Should save and retrieve an ItineraryPlanEntity successfully")
    @Test
    void saveAndRetrieveItineraryPlanEntity() {

        ItineraryPlanEntity entity = new ItineraryPlanEntity();
        entity.setTripSummary("Sample Itinerary");

        ItineraryPlanEntity savedEntity = itineraryPlanRepository.save(entity);
        long id = savedEntity.getId();

        Optional<ItineraryPlanEntity> retrievedEntity = itineraryPlanRepository.findById(id);

        assertThat(retrievedEntity).isPresent();
        assertThat(retrievedEntity.get().getTripSummary()).isEqualTo("Sample Itinerary");
    }

    @DisplayName("Should return empty when retrieving a non-existent ItineraryPlanEntity")
    @Test
    void retrieveNonExistentItineraryPlanEntity() {
        long nonExistentId = Random.from(RandomGenerator.getDefault()).nextLong(1, Long.MAX_VALUE);

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
        long id = savedEntity.getId();
        itineraryPlanRepository.deleteById(id);

        Optional<ItineraryPlanEntity> retrievedEntity = itineraryPlanRepository.findById(id);
        assertThat(retrievedEntity).isEmpty();
    }

    @Test
    @Transactional
    public void testBatchInsert() {
        ItineraryPlanEntity plan = new ItineraryPlanEntity();
        plan.setTripSummary("Test");

        List<DayPlanEntity> days = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DayPlanEntity day = new DayPlanEntity();
            day.setDayTitle("Day " + i);
            day.setItineraryPlan(plan);  // Set parent reference
            days.add(day);
        }
        plan.setDayPlans(days);

        ItineraryPlanEntity savedPlan = itineraryPlanRepository.saveAndFlush(plan);
        assertThat(savedPlan.getId()).isNotNull();
        assertThat(savedPlan.getDayPlans()).hasSize(10);
    }
}