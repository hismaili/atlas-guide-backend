package com.smarttours.atlasguidebackend.infrastructure.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.ItineraryPlanJpaRepository;
import com.smarttours.atlasguidebackend.infrastructure.repository.jpa.UserItineraryRequestJpaRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * One-time migration to backfill destination, startDate, tripDuration, and tripTitle
 * on existing ItineraryPlanEntity rows using data from the UserItineraryRequest JSON.
 *
 * Run with: mvn spring-boot:run -Dspring-boot.run.profiles=migrate,<your-other-profiles>
 */
@Component
@Profile("migrate")
public class ItineraryFieldsMigration implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ItineraryFieldsMigration.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ItineraryPlanJpaRepository planRepo;
    private final UserItineraryRequestJpaRepository requestRepo;

    public ItineraryFieldsMigration(ItineraryPlanJpaRepository planRepo,
                                    UserItineraryRequestJpaRepository requestRepo) {
        this.planRepo = planRepo;
        this.requestRepo = requestRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        LOG.info("=== Starting itinerary fields migration ===");

        List<ItineraryPlanEntity> allPlans = planRepo.findAll();
        int updated = 0;
        int skipped = 0;
        int linked = 0;
        int errors = 0;

        for (ItineraryPlanEntity plan : allPlans) {
            String tripId = plan.getTripId();
            if (tripId == null) {
                LOG.warn("Skipping plan id={} — no tripId", plan.getId());
                skipped++;
                continue;
            }

            // Skip if already populated
            if (plan.getDestination() != null && !plan.getDestination().isBlank()) {
                LOG.debug("Plan tripId={} already has destination='{}', skipping", tripId, plan.getDestination());
                skipped++;
                continue;
            }

            UserItineraryRequest request = requestRepo.findByTripId(tripId);
            if (request == null) {
                LOG.warn("No UserItineraryRequest found for tripId={}, skipping", tripId);
                skipped++;
                continue;
            }

            String requestJson = request.getItineraryRequestJson();
            if (requestJson == null || requestJson.isBlank()) {
                LOG.warn("Empty request JSON for tripId={}, skipping", tripId);
                skipped++;
                continue;
            }

            try {
                JsonNode node = objectMapper.readTree(requestJson);
                String destination = node.path("destination").asText("");
                String startDate = node.path("startDate").asText("");
                int tripDuration = node.path("tripDuration").asInt(0);

                plan.setDestination(destination);
                plan.setStartDate(startDate);
                plan.setTripDuration(tripDuration);

                if (plan.getTripTitle() == null || plan.getTripTitle().isBlank()) {
                    plan.setTripTitle(destination.isBlank() ? "Untitled Trip" : "Trip to " + destination);
                }

                planRepo.save(plan);
                updated++;
                LOG.info("Updated plan tripId={}: destination='{}', startDate='{}', duration={}",
                        tripId, destination, startDate, tripDuration);

                // Link the plan to the request if not already linked
                if (request.getItineraryPlanEntity() == null) {
                    request.setItineraryPlanEntity(plan);
                    requestRepo.save(request);
                    linked++;
                    LOG.info("Linked request id={} to plan tripId={}", request.getUserItineraryRequestId(), tripId);
                }

            } catch (Exception e) {
                LOG.error("Failed to migrate tripId={}: {}", tripId, e.getMessage());
                errors++;
            }
        }

        LOG.info("=== Migration complete: {} updated, {} linked, {} skipped, {} errors (out of {} total) ===",
                updated, linked, skipped, errors, allPlans.size());
    }
}
