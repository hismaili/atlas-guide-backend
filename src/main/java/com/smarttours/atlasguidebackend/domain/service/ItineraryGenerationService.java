package com.smarttours.atlasguidebackend.domain.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.utils.UserPromptBuilder;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ItineraryGenerationService {

    private static final Logger LOG = LoggerFactory.getLogger(ItineraryGenerationService.class);

    private final LLMService llmService;
    private final UserPromptBuilder promptBuilder;
    private final SseService sseService;

    private final UserItineraryPersistenceService userItineraryPersistenceService;

    @Autowired
    private ObjectMapper objectMapper;

    public ItineraryGenerationService(LLMService llmService,
                                      UserPromptBuilder promptBuilder,
                                      SseService sseService,
                                      UserItineraryPersistenceService userItineraryPersistenceService) {
        this.promptBuilder = promptBuilder;
        this.llmService = llmService;
        this.sseService = sseService;
        this.userItineraryPersistenceService = userItineraryPersistenceService;
    }

    @Async("smartToursTaskExecutor")
    public void createItineraryAsync(Map<String, Object> userDetails, String tripId, @Valid ItineraryRequest request) {

        try {
            LOG.info("Creating itinerary for request: {}", request);

            //Save the user request
            long requestId = userItineraryPersistenceService.saveItineraryRequest(userDetails, tripId, request);

            // Build the prompt (we will do this in the next main step)
            String systemPrompt = promptBuilder.buildSystemPrompt(request);
            String userPrompt = promptBuilder.buildUserPrompt(request);

            // Call the LLM
            ItineraryPlan plan = llmService.getItinerary(systemPrompt, userPrompt);

            LOG.info("Received itinerary plan: {}", objectMapper.writeValueAsString(plan));

            if(plan == null) {
                throw new IllegalStateException("Received null itinerary plan from LLM");
            }
            //validate that we received a complete plan
            plan.isComplete();
            // Once the task is done, send the result through the SSE service
            sseService.sendItinerary(tripId, plan);

            // Save the itinerary response
            userItineraryPersistenceService.saveItineraryPlan(requestId, plan);


            // --- MOCKED RESPONSE FOR NOW ---
            //return createMockPlan();
        } catch (Exception e) {
            LOG.error("Error creating itinerary for tripId {}: {}", tripId, e.getMessage(), e);
            sseService.sendError(tripId, "Failed to generate itinerary. "+ e.getMessage());
        }
    }
}
