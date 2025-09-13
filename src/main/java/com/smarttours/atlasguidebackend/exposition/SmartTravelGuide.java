package com.smarttours.atlasguidebackend.exposition;

import com.smarttours.atlasguidebackend.domain.service.ItineraryService;
import com.smarttours.atlasguidebackend.domain.service.SseService;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SmartTravelGuide {

    private static final Logger LOG = LoggerFactory.getLogger(SmartTravelGuide.class);

    private final ItineraryService itineraryService;
    private final SseService sseService;

    public SmartTravelGuide(ItineraryService itineraryService, SseService sseService) {
        this.itineraryService = itineraryService;
        this.sseService = sseService;
    }

    @GetMapping(path = "/subscribe/{tripId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToItinerary(@PathVariable String tripId) {
        return sseService.subscribe(tripId);
    }

    @PostMapping(value = "/itinerary", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateVisitPlan(@Valid @RequestBody ItineraryRequest itineraryRequest) {
        // Log the request for debugging purposes
        LOG.info("Received itinerary request: {}", itineraryRequest);
        ItineraryPlan entity;
        try {
            // Validate the request object
            if (itineraryRequest == null) {
                throw new IllegalArgumentException("Itinerary request cannot be null.");
            }

            // Generate a unique ID for this trip generation task
            String tripId = UUID.randomUUID().toString();

            // Start the generation process in the background. We don't wait for it.
            itineraryService.createItineraryAsync(itineraryRequest, tripId);

            // Immediately return the tripId to the client
            return ResponseEntity.ok(Map.of("tripId", tripId));
        } catch (Exception e) {
            LOG.error("Error building user itinerary plan: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to build user itinerary plan", e);
        }
    }

    private String mockedResponse() {
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
