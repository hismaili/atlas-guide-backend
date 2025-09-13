package com.smarttours.atlasguidebackend.infrastructure.repository;

import com.smarttours.atlasguidebackend.infrastructure.entities.DayPlanEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DayPlanRepositoryTest {

    @Autowired
    private DayPlanRepository dayPlanRepository;

    @DisplayName("Should save and retrieve a DayPlanEntity successfully")
    @Test
    void saveAndRetrieveDayPlanEntity() {
        DayPlanEntity entity = new DayPlanEntity();
        //entity.setId(id);
        entity.setDayTitle("Sample Day Plan");

        DayPlanEntity savedDayPlan = dayPlanRepository.save(entity);
        assertThat(savedDayPlan.getId()).isNotNull();
        UUID id = savedDayPlan.getId();

        Optional<DayPlanEntity> retrievedEntity = dayPlanRepository.findById(id);

        assertThat(retrievedEntity).isPresent();
        assertThat(retrievedEntity.get().getDayTitle()).isEqualTo("Sample Day Plan");
    }

    @DisplayName("Should return empty when retrieving a non-existent DayPlanEntity")
    @Test
    void retrieveNonExistentDayPlanEntity() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<DayPlanEntity> retrievedEntity = dayPlanRepository.findById(nonExistentId);

        assertThat(retrievedEntity).isEmpty();
    }

    @DisplayName("Should delete a DayPlanEntity successfully")
    @Test
    void deleteDayPlanEntity() {

        DayPlanEntity entity = new DayPlanEntity();
        entity.setDayTitle("Day Plan to Delete");

        DayPlanEntity savedEntity = dayPlanRepository.save(entity);
        assertThat(savedEntity.getId()).isNotNull();

        UUID id = savedEntity.getId();
        dayPlanRepository.deleteById(id);

        Optional<DayPlanEntity> retrievedEntity = dayPlanRepository.findById(id);
        assertThat(retrievedEntity).isEmpty();
    }
}