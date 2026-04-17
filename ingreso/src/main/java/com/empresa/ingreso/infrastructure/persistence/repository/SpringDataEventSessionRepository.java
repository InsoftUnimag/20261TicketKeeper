package com.empresa.ingreso.infrastructure.persistence.repository;

import com.empresa.ingreso.infrastructure.persistence.entity.EventSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEventSessionRepository extends JpaRepository<EventSessionEntity, Long> {
}
