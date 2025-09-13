package com.smarttours.atlasguidebackend.infrastructure.repository;

import com.smarttours.atlasguidebackend.infrastructure.entities.ItineraryPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItineraryPlanRepository extends JpaRepository<ItineraryPlanEntity, UUID> {
}
