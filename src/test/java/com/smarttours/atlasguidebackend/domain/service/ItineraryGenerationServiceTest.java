package com.smarttours.atlasguidebackend.domain.service;



import com.smarttours.atlasguidebackend.domain.repositories.ItineraryRepository;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.DayPlan;
import com.smarttours.atlasguidebackend.domain.user.output.Event;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.ItineraryPlanJpaRepository;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.UserItineraryRequestJpaRepository;
import com.smarttours.atlasguidebackend.utils.UserPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class,
        MockitoExtension.class})
public class ItineraryGenerationServiceTest {

    // @Mock creates a dummy implementation for a class.
    @MockitoBean
    private UserPromptBuilder promptBuilder;

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private LLMService llmService;

    // @InjectMocks creates an instance of ItineraryService and injects the mocks into it.
    @Autowired
    private ItineraryGenerationService itineraryGenerationService;

    private ItineraryRequest validRequest;

    @MockitoSpyBean
    private ItineraryRepository itineraryRepository;

    @MockitoBean
    private UserItineraryRequestJpaRepository userItineraryRequestJpaRepository;

    @MockitoBean
    private ItineraryPlanJpaRepository itineraryPlanJpaRepository;

    private final String systemPrompt = "SYSTEM_PROMPT";
    private final String userPrompt = "USER_PROMPT";

    @BeforeEach
    void setUp() {
        //itineraryService = new ItineraryService(llmService, promptBuilder, sseService, itineraryRepository);
        // Create a reusable valid request object for our tests.
        validRequest = new ItineraryRequest();
        validRequest.setDestination("Test Destination");
        validRequest.setStartDate(LocalDate.now().plusDays(1));
        validRequest.setTripDuration(3);


        Mockito.when(itineraryPlanJpaRepository.save(any())).thenAnswer(i -> {
                ItineraryPlanEntity argument = (ItineraryPlanEntity) i.getArgument(0);
                if(argument.getId() == null)
                    argument.setId(UUID.randomUUID());
                return argument;
            }
        );
        final Map<UUID, UserItineraryRequest> db = new HashMap<>();
        when(userItineraryRequestJpaRepository.save(any())).thenAnswer(i -> {
                    UserItineraryRequest argument = i.getArgument(0);
                    if(argument.getUserItineraryRequestId() == null) {
                        argument.setUserItineraryRequestId(UUID.randomUUID());
                    }
                    db.put(argument.getUserItineraryRequestId(), argument);
                    return argument;
                }
        );

        when(userItineraryRequestJpaRepository.getReferenceById(any())).thenAnswer(i -> {
                    UUID argument = i.getArgument(0);
                    return db.get(argument);
                }
        );
        // Define the behavior of our mocks
        when(promptBuilder.buildSystemPrompt(any(ItineraryRequest.class))).thenReturn(systemPrompt);
        when(promptBuilder.buildUserPrompt(any(ItineraryRequest.class))).thenReturn(userPrompt);

        SseEmitter sseEmitterMock = mock(SseEmitter.class);
        doNothing().when(sseService).sendItinerary(any(), any(ItineraryPlan.class));
        doNothing().when(sseService).sendError(any(), any(String.class));
        when(sseService.subscribe(any())).thenReturn(sseEmitterMock);
    }

    @Test
    void createItineraryAsync_ShouldBuildPromptsAndSendResultViaSse() {
        ItineraryPlan validPlan = new ItineraryPlan();
        validPlan.setTripSummary("Valid Plan trip summary");

        ArrayList<DayPlan> dayPlans = new ArrayList<>();
        ArrayList<Event> events = new ArrayList<>();
        events.add(new Event("Event 1", "10:00 AM", "12:00 PM", null));
        dayPlans.add(new DayPlan(1, LocalDate.now().plusDays(1), "Day 1 description", events));
        validPlan.setItinerary(dayPlans);

        when(llmService.getItinerary(eq(systemPrompt), eq(userPrompt))).thenReturn(validPlan);



        // --- Act ---
        try {
            String tripId = UUID.randomUUID().toString();
            itineraryGenerationService.createItineraryAsync(tripId, validRequest);
        } catch (Exception e) {
            fail("Unexpected exception during test setup: " + e.getMessage());
        }

        // --- Assert / Verify ---
        // Verify that the prompt builder was used correctly.
        verify(promptBuilder, times(1)).buildSystemPrompt(any(ItineraryRequest.class));
        verify(promptBuilder, times(1)).buildUserPrompt(eq(validRequest));

        // CRITICAL: Verify that the final result was sent to the SseService.
        verify(sseService, times(1)).sendItinerary(any(), any(ItineraryPlan.class));
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
            String tripId   = UUID.randomUUID().toString();
            itineraryGenerationService.createItineraryAsync(tripId, validRequest);
        } catch (Exception e) {
            fail("Unexpected exception during test setup: " + e.getMessage());
        }

        // --- Assert / Verify ---
        // Verify that the sendItinerary method was NEVER called because of the exception.
        verify(sseService, never()).sendItinerary(anyString(), any(ItineraryPlan.class));
        // In a real implementation, you might verify that an sseService.sendError() method was called instead.
    }

    @Test
    void createItineraryAsync_ShouldThrowExceptionWhenResponseIsNotComplete() {
        ItineraryPlan incompletePlan = new ItineraryPlan();
        incompletePlan.setTripSummary("Incomplete Plan trip summary");
        incompletePlan.setItinerary(new ArrayList<>());
        when(llmService.getItinerary(eq(systemPrompt), eq(userPrompt))).thenReturn(incompletePlan);
        // --- Act ---
        try {
            String tripId  = UUID.randomUUID().toString();
            itineraryGenerationService.createItineraryAsync(tripId, validRequest);
        } catch (Exception e) {
            fail("Unexpected exception during test setup: " + e.getMessage());
        }

        // --- Assert / Verify ---
        // Verify that the prompt builder was used correctly.
        verify(promptBuilder, times(1)).buildSystemPrompt(any(ItineraryRequest.class));
        verify(promptBuilder, times(1)).buildUserPrompt(eq(validRequest));

        // CRITICAL: Verify that the final result was sent to the SseService.
        verify(sseService, times(1)).sendError(any(), eq("Failed to generate itinerary. ItineraryPlan must contain at least one DayPlan."));
    }

    @TestConfiguration
    @ComponentScan(basePackages = {
            "com.smarttours.atlasguidebackend.domain.service",
            "com.smarttours.atlasguidebackend.infrastructure.repository"
    })
    static class TestConfig {
        // Additional test-specific beans can be defined here if needed.
    }
}