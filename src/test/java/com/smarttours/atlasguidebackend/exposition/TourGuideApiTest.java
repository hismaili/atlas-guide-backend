package com.smarttours.atlasguidebackend.exposition;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.service.ItineraryGenerationService;
import com.smarttours.atlasguidebackend.domain.service.LLMService;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.utils.WithMockJwtAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"ollama","test"})
public class TourGuideApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItineraryGenerationService itineraryGenerationService;

    @MockitoBean
    private LLMService llmService;
    String startDate = LocalDate.now().plusDays(30).format(java.time.format.DateTimeFormatter.ISO_DATE);

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER", "ROLE_ADMIN"})
    void whenValidInput_thenReturns200() throws Exception {

        Map<String, Object> userDetails = Map.of("preferred_username", "test-user", "email", "test-user@test.com", "ip", "0.0.0.0/16");
        doNothing().when(itineraryGenerationService).createItineraryAsync(
                eq(userDetails),
                anyString(),
                any(ItineraryRequest.class));

        Map<String, Object> validInput = new HashMap<>();
        validInput.put("destination", "Paris, France");
        validInput.put("startDate", startDate);
        validInput.put("tripDuration", 5);
       mockMvc.perform(
                post("/api/itinerary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").exists())
                .andExpect(jsonPath("$.tripId", isA(String.class)));

        verify(itineraryGenerationService, timeout(2000).times(1))
                .createItineraryAsync(any(Map.class), anyString(), any(ItineraryRequest.class));
    }

    @Test
    @WithMockJwtAuth(subject = "user123", authorities = {"ROLE_USER", "ROLE_ADMIN"})
    void whenMissingDestination_thenReturns400() throws Exception {
        Map<String, Object> invalidInput = new HashMap<>();
        // Destination is missing
        invalidInput.put("startDate", this.startDate);
        invalidInput.put("tripDuration", 5);

        mockMvc.perform(post("/api/itinerary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.destination").value("Destination cannot be blank."));
    }
}
