package com.smarttours.atlasguidebackend.exposition;

import com.smarttours.atlasguidebackend.domain.service.ItineraryRetrievalService;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ItineraryRetrievalController {

    private static final Logger LOG = LoggerFactory.getLogger(ItineraryRetrievalController.class);

    private final ItineraryRetrievalService itineraryRetrievalService;

    public ItineraryRetrievalController(ItineraryRetrievalService itineraryRetrievalService) {
        this.itineraryRetrievalService = itineraryRetrievalService;
    }

    @GetMapping(path = "/itineraries")
    public List<ItineraryPlan> getAllItineraries() {
        LOG.info("Retrieving all itineraries");
        return itineraryRetrievalService.retrieveAllItineraries();
    }
}
