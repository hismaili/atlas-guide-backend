package com.smarttours.atlasguidebackend.domain.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.smarttours.atlasguidebackend.domain.service.ItineraryService;
import com.smarttours.atlasguidebackend.domain.service.LLMService;
import com.smarttours.atlasguidebackend.domain.service.SseService;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.utils.UserPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// Use MockitoExtension to automatically handle mock creation.
@ExtendWith(MockitoExtension.class)
public class ItineraryServiceTest {

    // @Mock creates a dummy implementation for a class.
    @Mock
    private UserPromptBuilder promptBuilder;

    @Mock
    private SseService sseService;

    @Mock
    private LLMService llmService;

    // @InjectMocks creates an instance of ItineraryService and injects the mocks into it.

    private ItineraryService itineraryService;

    private ItineraryRequest validRequest;
    private final String tripId = "test-trip-id-123";

    @BeforeEach
    void setUp() {
        itineraryService = new ItineraryService(llmService, promptBuilder, sseService);
        // Create a reusable valid request object for our tests.
        validRequest = new ItineraryRequest();
        validRequest.setDestination("Test Destination");
        validRequest.setStartDate(LocalDate.now().plusDays(1));
        validRequest.setTripDuration(3);
    }

    @Test
    void createItineraryAsync_ShouldBuildPromptsAndSendResultViaSse() {
        // --- Arrange ---
        String systemPrompt = "SYSTEM_PROMPT";
        String userPrompt = "USER_PROMPT";
        ItineraryPlan mockPlan = new ItineraryPlan(/*...mock plan data...*/);

        // Define the behavior of our mocks
        when(promptBuilder.buildSystemPrompt(any(ItineraryRequest.class))).thenReturn(systemPrompt);
        when(promptBuilder.buildUserPrompt(any(ItineraryRequest.class))).thenReturn(userPrompt);
        when(llmService.getItinerary(eq(systemPrompt), eq(userPrompt))).thenReturn(mockPlan);

        // --- Act ---
        try {
            itineraryService.createItineraryAsync(validRequest, tripId);
        } catch (Exception e) {
            fail("Unexpected exception during test setup: " + e.getMessage());
        }

        // --- Assert / Verify ---
        // Verify that the prompt builder was used correctly.
        verify(promptBuilder, times(1)).buildSystemPrompt(any(ItineraryRequest.class));
        verify(promptBuilder, times(1)).buildUserPrompt(eq(validRequest));

        // CRITICAL: Verify that the final result was sent to the SseService.
        verify(sseService, times(1)).sendItinerary(eq(tripId), any(ItineraryPlan.class));
    }

    @Test
    void createItineraryAsync_WhenLlmServiceFails_ShouldNotSendSseEvent() {
        // --- Arrange ---
        // Configure the mock to throw an exception when called.
        when(llmService.getItinerary(anyString(), anyString()))
                .thenThrow(new RuntimeException("LLM generation failed"));

        // --- Act ---
        // We call the method, but we expect it to handle the exception internally.
        try {
            itineraryService.createItineraryAsync(validRequest, tripId);
        } catch (JsonProcessingException e) {
            fail("Unexpected exception during test setup: " + e.getMessage());
        }

        // --- Assert / Verify ---
        // Verify that the sendItinerary method was NEVER called because of the exception.
        verify(sseService, never()).sendItinerary(anyString(), any(ItineraryPlan.class));
        // In a real implementation, you might verify that an sseService.sendError() method was called instead.
    }
}