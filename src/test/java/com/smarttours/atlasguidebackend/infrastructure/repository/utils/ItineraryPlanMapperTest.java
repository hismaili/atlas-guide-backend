package com.smarttours.atlasguidebackend.infrastructure.repository.utils;

import com.smarttours.atlasguidebackend.domain.exceptions.UncompleteItineraryException;
import com.smarttours.atlasguidebackend.domain.user.output.DayPlan;
import com.smarttours.atlasguidebackend.domain.user.output.Event;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.infrastructure.entities.DayPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.EventEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItineraryPlanMapperTest {

    // --- itineraryPlanToEntity ---

    @Test
    void itineraryPlanToEntity_shouldMapAllContextFields() throws UncompleteItineraryException {
        ItineraryPlan plan = buildValidPlan();

        ItineraryPlanEntity entity = ItineraryPlanMapper.itineraryPlanToEntity(
                plan, "trip-123", "Marrakech", "2026-07-01", 7);

        assertEquals("trip-123", entity.getTripId());
        assertEquals("Marrakech", entity.getDestination());
        assertEquals("2026-07-01", entity.getStartDate());
        assertEquals(7, entity.getTripDuration());
        assertEquals("Trip to Marrakech", entity.getTripTitle());
        assertEquals("A great summary", entity.getTripSummary());
        assertNotNull(entity.getDayPlans());
        assertEquals(1, entity.getDayPlans().size());
    }

    @Test
    void itineraryPlanToEntity_shouldThrowWhenNoDayPlans() {
        ItineraryPlan plan = new ItineraryPlan();
        plan.setTripSummary("Summary");
        plan.setItinerary(new ArrayList<>());

        assertThrows(UncompleteItineraryException.class, () ->
                ItineraryPlanMapper.itineraryPlanToEntity(plan, "t1", "Paris", "2026-01-01", 3));
    }

    @Test
    void itineraryPlanToEntity_shouldThrowWhenItineraryIsNull() {
        ItineraryPlan plan = new ItineraryPlan();
        plan.setTripSummary("Summary");
        plan.setItinerary(null);

        assertThrows(UncompleteItineraryException.class, () ->
                ItineraryPlanMapper.itineraryPlanToEntity(plan, "t1", "Paris", "2026-01-01", 3));
    }

    // --- itineraryPlanEntityToDomain ---

    @Test
    void itineraryPlanEntityToDomain_shouldMapAllFieldsAndSetSavedTrue() {
        ItineraryPlanEntity entity = new ItineraryPlanEntity();
        entity.setTripId("trip-abc");
        entity.setTripTitle("My Trip");
        entity.setDestination("Rome");
        entity.setStartDate("2026-08-15");
        entity.setTripDuration(4);
        entity.setTripSummary("Rome summary");

        // Add a day plan with an event
        DayPlanEntity dayPlanEntity = DayPlanEntity.builder()
                .withDayNumber(1)
                .withItineraryPlan(entity)
                .build();
        EventEntity eventEntity = new EventEntity();
        eventEntity.setType("Visit");
        eventEntity.setStartTime("09:00 AM");
        eventEntity.setEndTime("11:00 AM");
        eventEntity.setDayPlan(dayPlanEntity);
        dayPlanEntity.setEvents(List.of(eventEntity));
        entity.setDayPlans(List.of(dayPlanEntity));

        ItineraryPlan domain = ItineraryPlanMapper.itineraryPlanEntityToDomain(entity);

        assertNotNull(domain);
        assertEquals("trip-abc", domain.getTripId());
        assertEquals("My Trip", domain.getTripTitle());
        assertEquals("Rome", domain.getDestination());
        assertEquals("2026-08-15", domain.getStartDate());
        assertEquals(4, domain.getTripDuration());
        assertTrue(domain.isSaved());
        assertEquals("Rome summary", domain.getTripSummary());
        assertEquals(1, domain.getItinerary().size());
        assertEquals(1, domain.getItinerary().get(0).getEvents().size());
    }

    @Test
    void itineraryPlanEntityToDomain_shouldReturnNullWhenEntityIsNull() {
        assertNull(ItineraryPlanMapper.itineraryPlanEntityToDomain(null));
    }

    @Test
    void itineraryPlanEntityToDomain_shouldHandleEmptyDayPlans() {
        ItineraryPlanEntity entity = new ItineraryPlanEntity();
        entity.setTripId("trip-empty");
        entity.setTripTitle("Empty Trip");
        entity.setDayPlans(null);

        ItineraryPlan domain = ItineraryPlanMapper.itineraryPlanEntityToDomain(entity);

        assertNotNull(domain);
        assertEquals("trip-empty", domain.getTripId());
        assertTrue(domain.isSaved());
        assertNull(domain.getItinerary());
    }

    // --- Helper ---

    private ItineraryPlan buildValidPlan() {
        ItineraryPlan plan = new ItineraryPlan();
        plan.setTripSummary("A great summary");

        ArrayList<Event> events = new ArrayList<>();
        events.add(new Event("Visit", "10:00 AM", "12:00 PM", null));
        ArrayList<DayPlan> dayPlans = new ArrayList<>();
        dayPlans.add(new DayPlan(1, LocalDate.of(2026, 7, 1), "Day 1 title", events));
        plan.setItinerary(dayPlans);
        return plan;
    }
}
