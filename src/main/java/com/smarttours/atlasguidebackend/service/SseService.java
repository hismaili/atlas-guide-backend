package com.smarttours.atlasguidebackend.service;


import com.smarttours.atlasguidebackend.user.output.ItineraryPlan;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    // A thread-safe map to store the emitters (connections) for each trip.
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Creates and stores a new SSE connection for a given trip ID.
     * @param tripId The unique ID for the trip generation task.
     * @return The SseEmitter object for the controller to return.
     */
    public SseEmitter subscribe(String tripId) {
        // Create an emitter with a long timeout
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Store the emitter so we can send data to it later
        this.emitters.put(tripId, emitter);

        // Handle what happens when the connection is closed or times out
        emitter.onCompletion(() -> this.emitters.remove(tripId));
        emitter.onTimeout(() -> this.emitters.remove(tripId));
        emitter.onError(e -> this.emitters.remove(tripId));

        return emitter;
    }

    /**
     * Sends the completed itinerary to the correct client.
     * @param tripId The ID of the trip that is now complete.
     * @param itineraryPlan The final ItineraryPlan object to send.
     */
    public void sendItinerary(String tripId, ItineraryPlan itineraryPlan) {
        SseEmitter emitter = this.emitters.get(tripId);
        if (emitter != null) {
            try {
                // Send the event. The "event" name can be used by the client to filter messages.
                emitter.send(SseEmitter.event()
                        .name("itinerary-complete")
                        .data(itineraryPlan));

                // Complete the connection, which closes it for the client.
                emitter.complete();
            } catch (IOException e) {
                // If there's an error (e.g., client disconnected), remove the emitter.
                this.emitters.remove(tripId);
            }
        }
    }

    public void sendError(String tripId, String errorMessage) {
        SseEmitter emitter = this.emitters.get(tripId);
        if (emitter != null) {
            try {
                // Send the event. The "event" name can be used by the client to filter messages.
                emitter.send(SseEmitter.event()
                        .name("itinerary-complete")
                        .data(errorMessage));

                // Complete the connection, which closes it for the client.
                emitter.complete();
            } catch (IOException e) {
                // If there's an error (e.g., client disconnected), remove the emitter.
                this.emitters.remove(tripId);
            }
        }
    }
}
