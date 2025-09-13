package com.smarttours.atlasguidebackend.exposition;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.service.ItineraryService;
import com.smarttours.atlasguidebackend.domain.service.LLMService;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private ItineraryService itineraryService;

    @MockitoBean
    private LLMService llmService;

    @Test
    void whenValidInput_thenReturns200() throws Exception {

        doNothing().when(itineraryService).createItineraryAsync(any(ItineraryRequest.class), anyString());

        Map<String, Object> validInput = new HashMap<>();
        validInput.put("destination", "Paris, France");
        validInput.put("startDate", "2025-09-15");
        validInput.put("tripDuration", 5);
        // Optional fields are not required for a valid request

        mockMvc.perform(post("/api/v1/itinerary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").exists())
                .andExpect(jsonPath("$.tripId", isA(String.class)));

        verify(itineraryService, timeout(2000).times(1))
                .createItineraryAsync(any(ItineraryRequest.class), anyString());
    }

    @Test
    void whenMissingDestination_thenReturns400() throws Exception {
        Map<String, Object> invalidInput = new HashMap<>();
        // Destination is missing
        invalidInput.put("startDate", "2025-09-15");
        invalidInput.put("tripDuration", 5);

        mockMvc.perform(post("/api/v1/itinerary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.destination").value("Destination cannot be blank."));
    }
}
