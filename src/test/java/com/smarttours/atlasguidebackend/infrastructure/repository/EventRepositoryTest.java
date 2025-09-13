package com.smarttours.atlasguidebackend.infrastructure.repository;

import com.smarttours.atlasguidebackend.infrastructure.entities.EventEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.VisitCardEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

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
        //UUID eventId = UUID.randomUUID();
        EventEntity event = new EventEntity();
        //event.setId(eventId);
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
        UUID nonExistentId = UUID.randomUUID();

        Optional<EventEntity> retrievedEvent = eventRepository.findById(nonExistentId);

        assertThat(retrievedEvent).isEmpty();
    }

    @DisplayName("Should delete an EventEntity successfully")
    @Test
    void deleteEventEntity() {
        // Create EventEntity
        //UUID eventId = UUID.randomUUID();
        EventEntity event = new EventEntity();
        //event.setId(eventId);
        event.setType("Activity");

        // Save and delete EventEntity
        EventEntity savedEntity = eventRepository.save(event);
        assertThat(savedEntity.getId()).isNotNull();
        UUID eventId = savedEntity.getId();
        eventRepository.deleteById(eventId);

        // Verify deletion
        Optional<EventEntity> retrievedEvent = eventRepository.findById(eventId);
        assertThat(retrievedEvent).isEmpty();
    }
}