package com.smarttours.atlasguidebackend.exposition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.service.ItineraryRetrievalService;
import com.smarttours.atlasguidebackend.domain.service.LLMService;
import com.smarttours.atlasguidebackend.domain.user.output.DayPlan;
import com.smarttours.atlasguidebackend.domain.user.output.Event;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.utils.WithMockJwtAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"ollama", "test"})
public class ItineraryRetrievalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItineraryRetrievalService itineraryRetrievalService;

    @MockitoBean
    private LLMService llmService;

    // --- GET /api/itineraries ---

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"},
            claims = {"preferred_username:testuser"})
    void getAllItineraries_shouldReturnListForAuthenticatedUser() throws Exception {
        ItineraryPlan plan = buildSamplePlan("trip-1", "Paris", true);
        when(itineraryRetrievalService.retrieveItinerariesByOwnerName("testuser"))
                .thenReturn(List.of(plan));

        mockMvc.perform(get("/api/itineraries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trip_id").value("trip-1"))
                .andExpect(jsonPath("$[0].destination").value("Paris"))
                .andExpect(jsonPath("$[0].is_saved").value(true));
    }

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"},
            claims = {"preferred_username:testuser"})
    void getAllItineraries_shouldReturnEmptyListWhenNoItineraries() throws Exception {
        when(itineraryRetrievalService.retrieveItinerariesByOwnerName("testuser"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/itineraries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllItineraries_shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/itineraries"))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /api/itineraries/{tripId} ---

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"})
    void getItineraryByTripId_shouldReturnPlanWhenFound() throws Exception {
        ItineraryPlan plan = buildSamplePlan("trip-42", "Tokyo", true);
        when(itineraryRetrievalService.retrieveItineraryByTripId("trip-42"))
                .thenReturn(plan);

        mockMvc.perform(get("/api/itineraries/trip-42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trip_id").value("trip-42"))
                .andExpect(jsonPath("$.destination").value("Tokyo"))
                .andExpect(jsonPath("$.trip_title").value("Trip to Tokyo"))
                .andExpect(jsonPath("$.trip_duration").value(5))
                .andExpect(jsonPath("$.is_saved").value(true))
                .andExpect(jsonPath("$.trip_summary").value("A great trip"));
    }

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"})
    void getItineraryByTripId_shouldReturn404WhenNotFound() throws Exception {
        when(itineraryRetrievalService.retrieveItineraryByTripId("nonexistent"))
                .thenReturn(null);

        mockMvc.perform(get("/api/itineraries/nonexistent"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/itineraries/{tripId} ---

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"})
    void deleteItinerary_shouldReturn204WhenDeleted() throws Exception {
        when(itineraryRetrievalService.deleteByTripId("trip-42"))
                .thenReturn(true);

        mockMvc.perform(delete("/api/itineraries/trip-42"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"})
    void deleteItinerary_shouldReturn404WhenNotFound() throws Exception {
        when(itineraryRetrievalService.deleteByTripId("nonexistent"))
                .thenReturn(false);

        mockMvc.perform(delete("/api/itineraries/nonexistent"))
                .andExpect(status().isNotFound());
    }

    // --- PATCH /api/itineraries/{tripId} ---

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"})
    void updateTripTitle_shouldReturn200WhenUpdated() throws Exception {
        ItineraryPlan updated = buildSamplePlan("trip-42", "Tokyo", true);
        updated.setTripTitle("My Custom Title");
        when(itineraryRetrievalService.updateTripTitle("trip-42", "My Custom Title"))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/itineraries/trip-42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "My Custom Title"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trip_title").value("My Custom Title"));
    }

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"})
    void updateTripTitle_shouldReturn404WhenTripNotFound() throws Exception {
        when(itineraryRetrievalService.updateTripTitle("nonexistent", "New Title"))
                .thenReturn(null);

        mockMvc.perform(patch("/api/itineraries/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "New Title"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"})
    void updateTripTitle_shouldReturn400WhenTitleMissing() throws Exception {
        mockMvc.perform(patch("/api/itineraries/trip-42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("other", "value"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER"})
    void updateTripTitle_shouldReturn400WhenTitleBlank() throws Exception {
        mockMvc.perform(patch("/api/itineraries/trip-42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "   "))))
                .andExpect(status().isBadRequest());
    }

    // --- Helper ---

    private ItineraryPlan buildSamplePlan(String tripId, String destination, boolean saved) {
        ItineraryPlan plan = new ItineraryPlan();
        plan.setTripId(tripId);
        plan.setTripTitle("Trip to " + destination);
        plan.setDestination(destination);
        plan.setStartDate("2026-06-01");
        plan.setTripDuration(5);
        plan.setSaved(saved);
        plan.setTripSummary("A great trip");

        ArrayList<Event> events = new ArrayList<>();
        events.add(new Event("Visit", "10:00 AM", "12:00 PM", null));
        ArrayList<DayPlan> dayPlans = new ArrayList<>();
        dayPlans.add(new DayPlan(1, LocalDate.of(2026, 6, 1), "Day 1", events));
        plan.setItinerary(dayPlans);
        return plan;
    }
}
