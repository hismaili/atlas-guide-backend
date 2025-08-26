package com.smarttours.atlasguidebackend.utils;


import com.smarttours.atlasguidebackend.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.user.input.TourGuidePersona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * A utility class responsible for building the user prompt for the LLM
 * from a validated ItineraryRequest object.
 */
@Component
public final class UserPromptBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(UserPromptBuilder.class);

    /*<OUTPUT_SCHEMA>
        {
          "trip_summary": "string",
          "itinerary": [
            {
              "day": "integer",
              "date": "string (YYYY-MM-DD)",
              "day_title": "string",
              "daily_summary": "string",
              "events": [
                {
                  "type": "string (attraction | meal | activity)",
                  "start_time": "string (HH:MM)",
                  "end_time": "string (HH:MM)",
                  "fiche_de_visite": {
                    "name": "string",
                    "category": "string",
                    "description": "string",
                    "address": "string",
                    "opening_hours": "string",
                    "estimated_duration_minutes": "integer",
                    "ticket_info": "string",
                    "why_its_for_you": "string",
                    "insider_tip": "string",
                    "logistics": "string"
                  }
                }
              ]
            }
          ]
        }
        </OUTPUT_SCHEMA>*/
    // The System Prompt is a constant template. It's the AI's "constitution".
    private static final String SYSTEM_PROMPT_TEMPLATE = """
        You are an elite, world-class travel expert named "Atlas." Your sole mission is to function as a Personal Tourism Guide engine. You combine the logistical genius of a seasoned travel agent with the local, on-the-ground knowledge of a city's best guide.

        You MUST follow all rules and constraints provided. Your final output MUST be a single, valid JSON object that adheres strictly to the provided schema. Do not include any explanatory text, markdown formatting, or any characters before or after the JSON structure.

        <RULES_AND_CONSTRAINTS>
            1.  **Strict Geo-Fencing (CRITICAL RULE):** ALL suggested locations (attractions, restaurants, etc.) MUST be located physically within the city limits of the user's specified **[Destination]**. You are explicitly forbidden from suggesting anything from other cities. For example, if the destination is Casablanca, you cannot suggest attractions in Marrakesh.
                   
            2.  **Internal Verification Step (CRITICAL RULE):** Before generating the final JSON, you MUST perform a silent, internal self-correction step.
                *   First, generate a draft itinerary.
                *   Second, for every single location in the draft, you MUST verify its physical address against the user's specified **[Destination]**. If it is not verifiably inside that city, you MUST replace it.
                *   Third, you MUST verify that any 'insider tips' or facts (like ceremonies, ticket prices, opening hours) are accurate and specific to that exact location. Discard and replace any generic or invented facts.
                   
            3.  **Geo-Logical Optimization:** For each day, you MUST group activities and sites that are geographically close to each other to minimize travel time within the **[Destination]**.
                   
            4.  **Full Start Date and Duration Adherence:** You MUST generate a complete itinerary for the entire **[Trip Duration]** starting from the **[Start Date]** specified by the user. 
            A 5-day request requires 5 days of output starting from the start date provided.
                   
            5.  **Smart Meal Integration:** You MUST include suggestions for lunch and dinner each day that are geographically convenient to the day's events.
                   
            6.  **Persona Adoption:** You MUST write all descriptive text in the persona provided in the user prompt.
                   
            7.  **Mandatory Full Event Details:** For every single object inside the `events` array, you MUST generate a complete and detailed `fiche_de_visite` object.
                   
            8.  **Feasibility Check:** If the user's requests are unrealistic for the given duration, you MUST state this clearly in the `trip_summary` and then generate the most logical and enjoyable alternative.
            </RULES_AND_CONSTRAINTS>
                   
            Your answer must be correctly and perfectly parsable using JSON.stringify, while keeping all the mandatory filled.
        """;

    /**
     * Returns the static system prompt.
     * In a more advanced system, this could be built dynamically as well.
     */
    public String buildSystemPrompt(ItineraryRequest request) {
        String systemPrompt =
        SYSTEM_PROMPT_TEMPLATE.replace("[Destination]", request.getDestination())
                .replace("[Start Date]", request.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .replace("[Trip Duration]", String.valueOf(request.getTripDuration()));
        LOG.info(SYSTEM_PROMPT_TEMPLATE);
        return SYSTEM_PROMPT_TEMPLATE;
    }

    /**
     * Builds the dynamic user prompt based on the incoming request.
     * It intelligently includes sections only if the relevant data is provided.
     *
     * @param request The user's ItineraryRequest DTO.
     * @return A fully formatted string ready to be sent to the LLM.
     */
    public String buildUserPrompt(ItineraryRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("Please generate a personalized travel itinerary based on the following trip details:\n\n");

        // --- Mandatory Fields ---
        sb.append(String.format("*   **Destination:** %s\n", request.getDestination()));

        if(request.getEndDate() != null && request.getEndDate().isAfter(request.getStartDate())) {
            sb.append(String.format("*   **Trip Dates:** Starting on %s for %d days\n", request.getStartDate(), request.getStartDate().until(request.getEndDate()).getDays()));
        } else {
            sb.append(String.format("*   **Trip Duration:** %d days\n", request.getTripDuration()));
        }


        // --- Optional Fields ---
        // We only append these sections if the user provided the data.

        if (request.getTravelerType() != null) {
            sb.append(String.format("*   **Traveler Profile:** %s\n", request.getTravelerType().getValue()));
        }

        if (request.getBudget() != null) {
            sb.append(String.format("*   **Daily Budget:** %s\n", request.getBudget().getValue()));
        }

        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            String interests = String.join(", ", request.getInterests());
            sb.append(String.format("*   **Primary Interests:** %s\n", interests));
        }

        if (request.getPace() != null) {
            sb.append(String.format("*   **Travel Pace:** %s\n", request.getPace().getValue()));
        }

        sb.append("\n");

        // --- Persona Instructions (Handle default logic here) ---
        TourGuidePersona finalPersona = (request.getPersona() != null)
                ? request.getPersona()
                : TourGuidePersona.FRIENDLY_LOCAL_EXPERT; // Infer default if not provided

        sb.append("**Persona Instructions for this Itinerary:**\n");
        sb.append(finalPersona.getInstructions());

        String userPrompt = sb.toString();
        LOG.info(userPrompt);
        return userPrompt;
    }
}