package com.smarttours.atlasguidebackend.infrastructure.repository.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttours.atlasguidebackend.domain.exceptions.UncompleteItineraryException;
import com.smarttours.atlasguidebackend.domain.user.output.DayPlan;
import com.smarttours.atlasguidebackend.domain.user.output.Event;
import com.smarttours.atlasguidebackend.domain.user.output.FicheDeVisite;
import com.smarttours.atlasguidebackend.domain.user.output.ItineraryPlan;
import com.smarttours.atlasguidebackend.infrastructure.entities.DayPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.EventEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import com.smarttours.atlasguidebackend.infrastructure.entities.VisitCardEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.smarttours.atlasguidebackend.infrastructure.entities.DayPlanEntity.builder;
import static java.util.stream.Collectors.toList;

public class ItineraryPlanMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_OPTIONALS, MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);
    }

    public static String itineraryPlanToJson(ItineraryPlan itineraryPlan) throws JsonProcessingException {
        return objectMapper.writeValueAsString(itineraryPlan);
    }

    public static ItineraryPlanEntity itineraryPlanToEntity(ItineraryPlan itineraryPlan, String tripId,
                                                               String destination, String startDate, int tripDuration) throws UncompleteItineraryException {
        ItineraryPlanEntity entity = new ItineraryPlanEntity();
        BeanUtils.copyProperties(itineraryPlan, entity);
        entity.setTripId(tripId);
        entity.setDestination(destination);
        entity.setStartDate(startDate);
        entity.setTripDuration(tripDuration);
        entity.setTripTitle("Trip to " + destination);

        List<DayPlanEntity> dayPlanEntities = validateAndGetItinerary(itineraryPlan, entity);
        entity.setDayPlans(dayPlanEntities);
        return entity;
    }

    private static List<DayPlanEntity> validateAndGetItinerary(ItineraryPlan itineraryPlan, ItineraryPlanEntity entity) throws UncompleteItineraryException {
        List<DayPlan> DayPlanList = itineraryPlan.getItinerary();
        if(CollectionUtils.isEmpty(DayPlanList)) {
            throw new UncompleteItineraryException("ItineraryPlan must contain at least one DayPlan");
        }
        return DayPlanList.stream().map(dayPlan ->
                {
                    try {

                        DayPlanEntity dayPlanEntity = builder()
                                .withDayNumber(dayPlan.getDay())
                                .withItineraryPlan(entity)
                                .build();
                        BeanUtils.copyProperties(dayPlan, dayPlanEntity);
                        dayPlanEntity.setEvents(mapDayEvents(dayPlan.getEvents(), dayPlanEntity));
                        return dayPlanEntity;
                    } catch (UncompleteItineraryException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).toList();
    }



    private static List<EventEntity> mapDayEvents(List<Event> dayPlanEvents, DayPlanEntity dayPlanEntity) throws UncompleteItineraryException {
        if(CollectionUtils.isEmpty(dayPlanEvents)) {
            throw new UncompleteItineraryException("Each DayPlan must contain at least one Event.");
        }
        return dayPlanEvents.stream().map(event -> {
            EventEntity eventEntity = new EventEntity();
            BeanUtils.copyProperties(event, eventEntity);
            if(event.getFicheDeVisite() != null) {
                eventEntity.setFicheDeVisite(mapVisitCard(event.getFicheDeVisite()));
            }
            eventEntity.setDayPlan(dayPlanEntity);
            return eventEntity;
        }).collect(toList());
    }

    private static VisitCardEntity mapVisitCard
            (FicheDeVisite ficheDeVisite) {
        VisitCardEntity visitCard = new VisitCardEntity();
        BeanUtils.copyProperties(ficheDeVisite, visitCard);
        return visitCard;

    }

    public static ItineraryPlan itineraryPlanEntityToDomain(ItineraryPlanEntity itineraryPlanEntity) {
        if (itineraryPlanEntity == null) {
            return null;
        }
        ItineraryPlan itineraryPlan = new ItineraryPlan();
        itineraryPlan.setTripId(itineraryPlanEntity.getTripId());
        itineraryPlan.setTripTitle(itineraryPlanEntity.getTripTitle());
        itineraryPlan.setDestination(itineraryPlanEntity.getDestination());
        itineraryPlan.setStartDate(itineraryPlanEntity.getStartDate());
        itineraryPlan.setTripDuration(itineraryPlanEntity.getTripDuration());
        itineraryPlan.setSaved(true);
        itineraryPlan.setTripSummary(itineraryPlanEntity.getTripSummary());
        if (!CollectionUtils.isEmpty(itineraryPlanEntity.getDayPlans())) {
            List<DayPlan> dayPlans = itineraryPlanEntity.getDayPlans().stream().map(dayPlanEntity -> {
                DayPlan dayPlan = new DayPlan();
                dayPlan.setDay(dayPlanEntity.getDayInPlan());
                BeanUtils.copyProperties(dayPlanEntity, dayPlan);
                if (!CollectionUtils.isEmpty(dayPlanEntity.getEvents())) {
                    List<Event> events = dayPlanEntity.getEvents().stream().map(eventEntity -> {
                        Event event = new Event();
                        BeanUtils.copyProperties(eventEntity, event);
                        if (eventEntity.getFicheDeVisite() != null) {
                            FicheDeVisite ficheDeVisite = new FicheDeVisite();
                            VisitCardEntity ficheDeVisiteEntity = eventEntity.getFicheDeVisite();
                            BeanUtils.copyProperties(ficheDeVisiteEntity, ficheDeVisite);
                            event.setFicheDeVisite(ficheDeVisite);
                        }
                        return event;
                    }).collect(toList());
                    dayPlan.setEvents(events);
                }
                return dayPlan;
            }).collect(toList());
            itineraryPlan.setItinerary(dayPlans);
        }
        return itineraryPlan;
    }
}
