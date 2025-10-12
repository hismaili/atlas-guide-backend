package com.smarttours.atlasguidebackend.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.user.input.Budget;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.domain.user.input.Pace;
import com.smarttours.atlasguidebackend.domain.user.input.TravelerType;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;

import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"ollama","test"})
public class ItineraryGenearationServiceIT {

    @MockitoSpyBean
    private ItineraryGenerationService itineraryGenerationService;

    @Mock
    private SseService sseService;

    @Mock
    private LLMService llmService;

    @Autowired
    private ObjectMapper objectMapper;


    String itinerary = """
    {"trip_summary":"This 7-day itinerary offers a balanced exploration of San Francisco’s historical and cultural highlights, incorporating iconic landmarks such as the Golden Gate Bridge, and significant historical sites. The pacing is designed to accommodate a moderate travel tempo, allowing for ample time for both structured activities and spontaneous discoveries.  Alternative trip length suggestions include a 5-day trip focusing on core attractions, or a 10-day itinerary incorporating day trips to nearby destinations like Napa Valley or Muir Woods.","itinerary":[{"day":1,"date":"2024-03-16","day_title":"San Francisco – Maritime Heritage","daily_summary":"Day 1: Arrival in San Francisco – Exploration of Historic Waterfront and Culinary Delights.","events":[{"type":"Arrival & Hotel Check-in","start_time":"09:00","end_time":null,"fiche_de_visite":{"securityDescription":"Standard hotel security measures.","nearByTransportations":["Embarcadero BART Station","Montgomery Street Cable Car Stop"],"yourResponseAccuracy":"Verified","name":"Hotel Nikko San Francisco","category":"Accommodation","description":"Check in to your hotel, situated in the Financial District.","address":"160 Spear Street, San Francisco, CA 94105","opening_hours":"24/7","estimated_duration_minutes":60,"ticket_info":"N/A","why_its_for_you":"Convenient location for exploring San Francisco's historic waterfront.","insider_tip":"Consider hotels near the Embarcadero for easy access to the waterfront.","logistics":"Transportation: Airport shuttle or taxi to hotel."}},{"type":"Fisherman's Wharf & Pier 39","start_time":"11:00","end_time":null,"fiche_de_visite":{"securityDescription":"Standard tourist area security.","nearByTransportations":["Embarcadero BART Station","Powell Street Cable Car Stop"],"yourResponseAccuracy":"Verified","name":"Fisherman's Wharf","category":"Tourist Attraction","description":"Explore the iconic Fisherman's Wharf, observing the sea lions at Pier 39.","address":"Pier 39, San Francisco, CA 94133","opening_hours":"08:00 – 20:00","estimated_duration_minutes":90,"ticket_info":"N/A","why_its_for_you":"Experience the quintessential San Francisco waterfront.","insider_tip":"Visit during early morning to avoid peak crowds.","logistics":"Walking distance from hotel."}},{"type":"Lunch – Scoma's","start_time":"13:00","end_time":null,"fiche_de_visite":{"securityDescription":"Standard restaurant security.","nearByTransportations":["Embarcadero BART Station","Powell Street Cable Car Stop"],"yourResponseAccuracy":"Verified","name":"Scoma's","category":"Restaurant","description":"Enjoy fresh seafood at Scoma's, a San Francisco institution.","address":"777 Point Reyes Ave, San Francisco, CA 94103","opening_hours":"11:30 – 21:00","estimated_duration_minutes":90,"ticket_info":"N/A","why_its_for_you":"Authentic San Francisco seafood dining experience.","insider_tip":"Make reservations in advance, particularly during peak season.","logistics":"Located on Fisherman's Wharf."}}]},{"day":2,"date":"2024-03-17","day_title":"Golden Gate & Presidio","daily_summary":"Day 2: Golden Gate Bridge & Presidio National Park – Iconic Landmarks and Military History.","events":[{"type":"Golden Gate Bridge Exploration","start_time":"09:00","end_time":null,"fiche_de_visite":{"securityDescription":"Standard landmark security.","nearByTransportations":["Fort Point BART Station","Golden Gate Bridge Welcome Center"],"yourResponseAccuracy":"Verified","name":"Golden Gate Bridge","category":"Landmark","description":"Walk or cycle across the Golden Gate Bridge, enjoying panoramic views.","address":"Golden Gate Bridge Vista Point, San Francisco, CA 94129","opening_hours":"24/7","estimated_duration_minutes":120,"ticket_info":"N/A","why_its_for_you":"Witnessing one of the world’s most iconic landmarks.","insider_tip":"Dress warmly, as temperatures near the bridge can be significantly cooler.","logistics":"Accessible by car, bike, or public transportation."}},{"type":"Lunch – Green Apple Restaurant","start_time":"13:00","end_time":null,"fiche_de_visite":{"securityDescription":"Standard restaurant security.","nearByTransportations":["Van Ness Avenue Cable Car Stop","Fort Point BART Station"],"yourResponseAccuracy":"Verified","name":"Green Apple Restaurant","category":"Restaurant","description":"Enjoy a casual lunch at Green Apple, known for its fresh salads and sandwiches.","address":"460 Fort Mason Drive, San Francisco, CA 94123","opening_hours":"11:00 – 18:00","estimated_duration_minutes":90,"ticket_info":"N/A","why_its_for_you":"Convenient lunch spot within the Presidio.","insider_tip":"Try the seasonal salads for the best experience.","logistics":"Located within the Presidio National Park."}},{"type":"Presidio National Park – Fort Point","start_time":"15:00","end_time":null,"fiche_de_visite":{"securityDescription":"Standard historic site security.","nearByTransportations":["Van Ness Avenue Cable Car Stop","Golden Gate Bridge Welcome Center"],"yourResponseAccuracy":"Verified","name":"Fort Point","category":"Historical Site","description":"Explore Fort Point, a Civil War-era fort located at the foot of the Golden Gate Bridge.","address":"20 Fort Point Way, San Francisco, CA 94129","opening_hours":"7:00 – 17:00","estimated_duration_minutes":60,"ticket_info":"N/A","why_its_for_you":"Significant Civil War history and breathtaking bridge views.","insider_tip":"Take photos from the base of the bridge for stunning views.","logistics":"Accessible by car, bike, or foot."}}]}]}
    """;


    @Test
    public void testGenerateItinerary() {

        doNothing().when(sseService).sendItinerary(anyString(), any());
        doNothing().when(sseService).sendError(anyString(), any());
        try {
            ItineraryPlan value = objectMapper.readValue(itinerary, ItineraryPlan.class);
            when(llmService.getItinerary(anyString(), anyString()))
                    .thenReturn(value);

            ItineraryRequest itineraryRequest = new ItineraryRequest();
            itineraryRequest.setDestination( "Rome");
            itineraryRequest.setStartDate(LocalDate.now().plusDays(15));
            itineraryRequest.setTripDuration(3);
            itineraryRequest.setTravelerType(TravelerType.FAMILY_WITH_TEENAGERS);
            itineraryRequest.setPace(Pace.ACTION_PACKED);
            itineraryRequest.setBudget(Budget.MODERATE);

            String tripId = "test-trip-id-123";
            this.itineraryGenerationService.createItineraryAsync(tripId, itineraryRequest);
        } catch (JsonProcessingException e) {
            fail("Failed to parse itinerary JSON: " + e.getMessage());
        } catch(Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }


    }

    @TestConfiguration
    @ComponentScan(basePackages = {
            "com.smarttours.atlasguidebackend.domain.service",
            "com.smarttours.atlasguidebackend.infrastructure"
    })
    static class TestConfig {
        // Additional test-specific beans can be defined here if needed.
    }
}
