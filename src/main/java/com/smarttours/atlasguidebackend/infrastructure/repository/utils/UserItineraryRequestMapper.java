package com.smarttours.atlasguidebackend.infrastructure.repository.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.exceptions.UncompleteItineraryException;
import com.smarttours.atlasguidebackend.domain.user.input.*;
import com.smarttours.atlasguidebackend.domain.user.output.DayPlan;
import com.smarttours.atlasguidebackend.domain.user.output.Event;
import com.smarttours.atlasguidebackend.domain.user.output.FicheDeVisite;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.infrastructure.entities.DayPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.EventEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.VisitCardEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.smarttours.atlasguidebackend.infrastructure.entities.DayPlanEntity.*;
import static java.util.stream.Collectors.toList;

public class UserItineraryRequestMapper {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_OPTIONALS, MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);
    }

    public static ItineraryRequest toDomain(com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest entity) throws Exception {
        if (entity == null) {
            return null;
        }
        ItineraryRequest itineraryRequest = new ItineraryRequest();
        String itineraryRequestJson = entity.getItineraryRequestJson();
        try {
            JsonNode itineraryRequestTree = objectMapper.readTree(itineraryRequestJson);
            itineraryRequest.setDestination(validString(itineraryRequestTree.path("destination").asText()));
            itineraryRequest.setStartDate(validDate(validString(itineraryRequestTree.path("startDate").asText())));
            itineraryRequest.setTripDuration(itineraryRequestTree.path("tripDuration").asInt());
            itineraryRequest.setEndDate(validDate(validString(itineraryRequestTree.path("endDate").asText())));
            itineraryRequest.setInterests(objectMapper.convertValue(itineraryRequestTree.path("interests"), objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)));
            itineraryRequest.setBudget(Budget.valueOf(validString(itineraryRequestTree.path("budget").asText())));
            itineraryRequest.setAccommodationLocation(validString(itineraryRequestTree.path("accommodationLocation").asText()));
            itineraryRequest.setTravelerType(TravelerType.valueOf(itineraryRequestTree.path("travelerType").asText()));
            itineraryRequest.setMustSeeList(objectMapper.convertValue(itineraryRequestTree.path("mustSeeList"), objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)));
            itineraryRequest.setAvoidList(objectMapper.convertValue(itineraryRequestTree.path("avoidList"), objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)));
            itineraryRequest.setAccessibilityNeeds(validString(itineraryRequestTree.path("accessibilityNeeds").asText()));
            itineraryRequest.setDailyStartTime(validTime(validString(itineraryRequestTree.path("dailyStartTime").asText())));
            itineraryRequest.setDietaryNeeds(validString(itineraryRequestTree.path("dietaryNeeds").asText()));
            itineraryRequest.setPace(Pace.valueOf(validString(itineraryRequestTree.path("pace").asText())));
            itineraryRequest.setPersona(TourGuidePersona.valueOf(validString(itineraryRequestTree.path("persona").asText())));
            itineraryRequest.setSpecialOccasion(validString(itineraryRequestTree.path("specialOccasion").asText()));
            itineraryRequest.setTransportationPrefs(objectMapper.convertValue(itineraryRequestTree.path("transportationPrefs"), objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)));
            return itineraryRequest;
        } catch (JsonProcessingException e) {
            throw new Exception(e);
        }


    }

    private static LocalTime validTime(String inputTime) {
        if (StringUtils.isEmpty(inputTime)) {
            return null;
        }
        try {
            return LocalTime.parse(inputTime);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate validDate(String inputDate) {
        if (StringUtils.isEmpty(inputDate)) {
            return null;
        }
        try {
            return LocalDate.parse(inputDate);
        } catch (Exception e) {
            return null;
        }
    }

    private static String validString(String input) {
        return StringUtils.isEmpty(input) ? StringUtils.EMPTY : input;
    }

    public static com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest toEntity(ItineraryRequest itineraryInputRequest, String tripId) throws JsonProcessingException {
        if (itineraryInputRequest == null) {
            return null;
        }
        com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest  entity = new com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest();
        entity.setItineraryRequestJson(objectMapper.writeValueAsString(itineraryInputRequest));
        entity.setTripId(tripId);
        return entity;
    }
}
