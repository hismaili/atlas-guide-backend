package com.smarttours.atlasguidebackend.infrastructure.repository.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smarttours.atlasguidebackend.domain.user.input.ItineraryRequest;
import com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserItineraryRequestMapperTest {

    @Test
    void toEntity_shouldMapAllUserDetailsCorrectly() throws JsonProcessingException {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("preferred_username", "john");
        userDetails.put("email", "john@test.com");
        userDetails.put("ip", "192.168.1.1");

        ItineraryRequest request = buildRequest();

        UserItineraryRequest entity = UserItineraryRequestMapper.toEntity(userDetails, request, "trip-99");

        assertNotNull(entity);
        assertEquals("trip-99", entity.getTripId());
        assertEquals("john", entity.getBlongsTo().getName());
        assertEquals("john@test.com", entity.getBlongsTo().getEmail());
        assertEquals("192.168.1.1", entity.getBlongsTo().getIpAddress());
        assertNotNull(entity.getItineraryRequestJson());
    }

    @Test
    void toEntity_shouldUseDefaultsWhenClaimsAreMissing() throws JsonProcessingException {
        Map<String, Object> userDetails = new HashMap<>();
        // No preferred_username, email, or ip

        ItineraryRequest request = buildRequest();

        UserItineraryRequest entity = UserItineraryRequestMapper.toEntity(userDetails, request, "trip-100");

        assertNotNull(entity);
        assertEquals("unknown", entity.getBlongsTo().getName());
        assertEquals("unknown", entity.getBlongsTo().getEmail());
        assertEquals("unknown", entity.getBlongsTo().getIpAddress());
    }

    @Test
    void toEntity_shouldUseDefaultsWhenClaimsAreNull() throws JsonProcessingException {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("preferred_username", null);
        userDetails.put("email", null);
        userDetails.put("ip", null);

        ItineraryRequest request = buildRequest();

        UserItineraryRequest entity = UserItineraryRequestMapper.toEntity(userDetails, request, "trip-101");

        assertNotNull(entity);
        assertEquals("unknown", entity.getBlongsTo().getName());
        assertEquals("unknown", entity.getBlongsTo().getEmail());
        assertEquals("unknown", entity.getBlongsTo().getIpAddress());
    }

    @Test
    void toEntity_shouldReturnNullWhenRequestIsNull() throws JsonProcessingException {
        Map<String, Object> userDetails = Map.of("preferred_username", "test");
        assertNull(UserItineraryRequestMapper.toEntity(userDetails, null, "trip-x"));
    }

    private ItineraryRequest buildRequest() {
        ItineraryRequest request = new ItineraryRequest();
        request.setDestination("London");
        request.setStartDate(LocalDate.of(2026, 9, 1));
        request.setTripDuration(3);
        return request;
    }
}
