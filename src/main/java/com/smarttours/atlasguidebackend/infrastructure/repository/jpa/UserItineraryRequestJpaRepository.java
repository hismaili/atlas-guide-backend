package com.smarttours.atlasguidebackend.infrastructure.repository.jpa;

import com.smarttours.atlasguidebackend.infrastructure.entities.UserItineraryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserItineraryRequestJpaRepository extends JpaRepository<UserItineraryRequest, UUID> {
}
