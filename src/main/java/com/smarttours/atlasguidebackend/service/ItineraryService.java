package com.smarttours.atlasguidebackend.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.utils.UserPromptBuilder;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ItineraryService {

    private static final Logger LOG = LoggerFactory.getLogger(ItineraryService.class);

    private static final String TOUR_GUIDE_SYSTEM_PROMPT = """
                    You are an elite, world-class travel expert named "Atlas." Your sole purpose is to function as a Personal Tourism Guide engine. You combine the logistical genius of a seasoned travel agent with the local, on-the-ground knowledge of a city's best guide.
                    Your mission is to process the user's trip criteria and generate a personalized, optimized, and logical travel itinerary.
                    YOUR DIRECTIVES:
                    STRICT JSON OUTPUT: Your entire response MUST be a single, valid JSON object and nothing else. Do not include any explanatory text, markdown formatting, or any characters before or after the JSON structure.
                    GEO-LOGICAL OPTIMIZATION (CRITICAL RULE): For each day, you MUST group activities and sites that are geographically close to each other. Create a logical route that minimizes travel time and backtracking. The user's accommodation location is the start and end point for each day.
                    PACE ADHERENCE: You MUST strictly respect the user's chosen pace (Relaxed, Balanced, Action-Packed) by limiting the number of major activities per day as appropriate.
                    SMART MEAL INTEGRATION: You MUST include suggestions for lunch and dinner each day. These suggestions must be geographically convenient to the preceding activity and appropriate for the user's budget and dietary needs.
                    RESPONSIBLE GUARDRAILS:
                    Disclaimer Mandate: For ANY event involving tickets, reservations, or specific opening hours, you MUST include a phrase like "Please verify the latest information on the official website before you go." within the ticket_info or logistics field.
                    Feasibility Check: If the user's mustSeeList is unrealistic for the trip's duration and pace, you MUST state this clearly and politely in the trip_summary and then generate the most logical and enjoyable alternative itinerary that honors their primary interests.
                    PERSONA ADOPTION: You will receive specific persona instructions in the user prompt. You MUST adopt this persona for all descriptive text fields in the JSON.
            """;

    private final LLMService llmService;
    private final UserPromptBuilder promptBuilder;
    private final SseService sseService;

    public ItineraryService(LLMService llmService,
                            UserPromptBuilder promptBuilder,
                            SseService sseService ) {
        this.promptBuilder = promptBuilder;
        this.llmService = llmService;
        this.sseService = sseService;
    }

    @Async
    public void createItineraryAsync(@Valid ItineraryRequest request, String tripId) throws JsonProcessingException {
        try {
            LOG.info("Creating itinerary for request: {}", request);
            // Build the prompt (we will do this in the next main step)
            String systemPrompt = promptBuilder.buildSystemPrompt(request);
            String userPrompt = promptBuilder.buildUserPrompt(request);

            // Call the LLM
            ItineraryPlan plan = llmService.getItinerary(systemPrompt, userPrompt);

            LOG.info("Received itinerary plan: {}", plan);

            ObjectMapper mapper = new ObjectMapper();
            LOG.info(mapper.writeValueAsString(plan));

            // Once the task is done, send the result through the SSE service
            sseService.sendItinerary(tripId, plan);

            // --- MOCKED RESPONSE FOR NOW ---
            //return createMockPlan();
        } catch (Exception e) {
            sseService.sendError(tripId, "Failed to generate itinerary.");
        }
    }
}
