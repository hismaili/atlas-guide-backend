package com.smarttours.atlasguidebackend.exposition;

import com.smarttours.atlasguidebackend.domain.service.ItineraryRetrievalService;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ItineraryRetrievalController {

    private static final Logger LOG = LoggerFactory.getLogger(ItineraryRetrievalController.class);

    private final ItineraryRetrievalService itineraryRetrievalService;

    public ItineraryRetrievalController(ItineraryRetrievalService itineraryRetrievalService) {
        this.itineraryRetrievalService = itineraryRetrievalService;
    }

    @GetMapping(path = "/itineraries")
    public List<ItineraryPlan> getAllItineraries(Authentication authentication) {
        String username = extractPreferredUsername(authentication);
        LOG.info("Retrieving all itineraries for user: {}", username);
        return itineraryRetrievalService.retrieveItinerariesByOwnerName(username);
    }

    private String extractPreferredUsername(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String username = jwtAuth.getToken().getClaimAsString("preferred_username");
            return username != null ? username : authentication.getName();
        }
        if (authentication instanceof OAuth2AuthenticationToken oauth2Auth) {
            Object username = oauth2Auth.getPrincipal().getAttribute("preferred_username");
            return username != null ? username.toString() : authentication.getName();
        }
        return authentication.getName();
    }

    @GetMapping(path = "/itineraries/{tripId}")
    public ResponseEntity<ItineraryPlan> getItineraryByTripId(@PathVariable String tripId) {
        LOG.info("Retrieving itinerary for tripId: {}", tripId);
        ItineraryPlan plan = itineraryRetrievalService.retrieveItineraryByTripId(tripId);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(plan);
    }

    @DeleteMapping(path = "/itineraries/{tripId}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable String tripId) {
        LOG.info("Deleting itinerary for tripId: {}", tripId);
        boolean deleted = itineraryRetrievalService.deleteByTripId(tripId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/itineraries/{tripId}")
    public ResponseEntity<ItineraryPlan> updateTripTitle(@PathVariable String tripId,
                                                         @RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        LOG.info("Updating title for tripId: {} to: {}", tripId, title);
        ItineraryPlan updated = itineraryRetrievalService.updateTripTitle(tripId, title);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}
