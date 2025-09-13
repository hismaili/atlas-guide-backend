package com.smarttours.atlasguidebackend.infrastructure.repository;

import com.smarttours.atlasguidebackend.infrastructure.entities.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
}