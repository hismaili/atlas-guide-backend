package com.smarttours.atlasguidebackend.domain.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.user.input.Pace;
import com.smarttours.atlasguidebackend.domain.user.input.TourGuidePersona;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class LLMService {

    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);
    private final ChatClient chatClient;

    LLMService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    ItineraryPlan getItinerary(String systemPrompt, String userPrompt) {
        try {
            return chatClient.prompt()
                    //.advisors(new SimpleLoggerAdvisor())
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(ItineraryPlan.class);
        } catch (Exception e) {
            // Log the exception (you can use a logging framework here)
            logger.error("Error calling LLM: " + e.getMessage(), e);

        }
        return null;
    }

    private Pace infereOptionalPace(Pace pace) {
        return pace != null ? pace : Pace.BALANCED; // Default to BALANCED if not provided
    }

    /**
     * Infers the persona from the request, defaulting to FRIENDLY_LOCAL_EXPERT if not provided.
     *
     * The idea of this method is to try to smartly deduce the tour guide's persona
     * based on the user's characteristics like : age, gender, nationality, ethnology, etc
     * and the other user's preferences (must see list, dietary diet, etc).
     * If the user has specified a persona, no inference is done
     *
     * @param persona The Persona provided by the user.
     * @return The inferred TourGuidePersona.
     */
    //TODO implement the user persona inference algorithm
    private static TourGuidePersona infereOptionalPersona(TourGuidePersona persona) {
        return (persona != null) ? persona : TourGuidePersona.FRIENDLY_LOCAL_EXPERT;
    }

    ItineraryPlan createMockPlan() throws JsonProcessingException {
        // Return a valid, hardcoded ItineraryPlan object for testing the flow.
        // This should be the same structure as your Flutter model.
        return new ObjectMapper().readValue(mockedJsonResponse(), ItineraryPlan.class);
    }

    private String mockedJsonResponse() {
        return """
                {
                  "trip_summary": "This itinerary offers a balanced mix of history, art, shopping, and Parisian atmosphere, perfectly tailored to your interests and pace. We’ve prioritized geographically close activities to maximize your time and minimize travel.  Given your desire to visit the Louvre and Eiffel Tower, this itinerary focuses on alternative, equally captivating experiences while still allowing you to enjoy the vibrant essence of Paris.  It’s a fantastic starting point – feel free to adjust it based on your evolving preferences!",
                  "itinerary": [
                    {
                      "day": 1,
                      "date": "2025-08-16",
                      "day_title": "History & Marais Charm",
                      "daily_summary": "Let's kick off our Parisian adventure! We’ll immerse ourselves in history and explore the Marais district, a fantastic blend of old and new.",
                      "events": [
                        {
                          "type": "Visit",
                          "start_time": "11:00 AM",
                          "end_time": null,
                          "fiche_de_visite": null
                        },
                        {
                          "type": "Lunch",
                          "start_time": "1:00 PM",
                          "end_time": null,
                          "fiche_de_visite": null
                        },
                        {
                          "type": "Explore",
                          "start_time": "2:30 PM",
                          "end_time": null,
                          "fiche_de_visite": null
                        }
                      ]
                    },
                    {
                      "day": 2,
                      "date": "2025-08-17",
                      "day_title": "Art & Shopping",
                      "daily_summary": "Today, we're diving into art and shopping – and maybe a little bit of late-night fun!",
                      "events": [
                        {
                          "type": "Visit",
                          "start_time": "11:00 AM",
                          "end_time": null,
                          "fiche_de_visite": null
                        },
                        {
                          "type": "Lunch",
                          "start_time": "2:00 PM",
                          "end_time": null,
                          "fiche_de_visite": null
                        },
                        {
                          "type": "Shopping",
                          "start_time": "3:30 PM",
                          "end_time": null,
                          "fiche_de_visite": null
                        },
                        {
                          "type": "Dinner",
                          "start_time": "7:00 PM",
                          "end_time": null,
                          "fiche_de_visite": null
                        }
                      ]
                    },
                    {
                      "day": 3,
                      "date": "2025-08-18",
                      "day_title": "Farewell Paris",
                      "daily_summary": "Our final day – let's soak up the Parisian atmosphere and revisit some favorite spots!",
                      "events": [
                        {
                          "type": "Visit",
                          "start_time": "11:00 AM",
                          "end_time": null,
                          "fiche_de_visite": null
                        },
                        {
                          "type": "Lunch",
                          "start_time": "1:00 PM",
                          "end_time": null,
                          "fiche_de_visite": null
                        },
                        {
                          "type": "Explore",
                          "start_time": "3:00 PM",
                          "end_time": null,
                          "fiche_de_visite": null
                        }
                      ]
                    }
                  ]
                }
                """;
    }
}
