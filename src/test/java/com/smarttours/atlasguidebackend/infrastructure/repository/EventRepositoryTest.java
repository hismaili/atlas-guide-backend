package com.smarttours.atlasguidebackend.infrastructure.repository;

import com.smarttours.atlasguidebackend.infrastructure.entities.EventEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.VisitCardEntity;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @DisplayName("Should save and retrieve an EventEntity with its VisitCardEntity successfully")
    @Test
    void saveAndRetrieveEventEntityWithVisitCard() {
        // Create VisitCardEntity
        VisitCardEntity visitCard = new VisitCardEntity();
        visitCard.setName("Sample Visit Card");
        visitCard.setDescription("Description of the visit card");

        // Create EventEntity
        UUID eventId = UUID.randomUUID();
        EventEntity event = new EventEntity();
        event.setType("Attraction");
        event.setStartTime("10:00 AM");
        event.setEndTime("12:00 PM");
        event.setFicheDeVisite(visitCard);

        // Save EventEntity
        EventEntity savedEvent = eventRepository.save(event);
        assertThat(savedEvent.getId()).isNotNull();

        // Retrieve EventEntity
        Optional<EventEntity> retrievedEvent = Optional.of(eventRepository.getReferenceById(savedEvent.getId()));
        assertThat(retrievedEvent).isPresent();
        assertThat(retrievedEvent.get().getType()).isEqualTo("Attraction");
        assertThat(retrievedEvent.get().getFicheDeVisite()).isNotNull();
        assertThat(retrievedEvent.get().getFicheDeVisite().getName()).isEqualTo("Sample Visit Card");
    }

    @DisplayName("Should return empty when retrieving a non-existent EventEntity")
    @Test
    void retrieveNonExistentEventEntity() {
        long nonExistentId = Random.from(RandomGenerator.getDefault()).nextLong(1, Long.MAX_VALUE);

        Optional<EventEntity> retrievedEvent = eventRepository.findById(nonExistentId);

        assertThat(retrievedEvent).isEmpty();
    }

    @DisplayName("Should delete an EventEntity successfully")
    @Test
    void deleteEventEntity() {
        // Create EventEntity
        long eventId = Random.from(RandomGenerator.getDefault()).nextLong(1, Long.MAX_VALUE);
        EventEntity event = new EventEntity();
        event.setType("Activity");

        // Save and delete EventEntity
        EventEntity savedEntity = eventRepository.save(event);
        eventRepository.deleteById(eventId);

        // Verify deletion
        Optional<EventEntity> retrievedEvent = eventRepository.findById(eventId);
        assertThat(retrievedEvent).isEmpty();
    }
}