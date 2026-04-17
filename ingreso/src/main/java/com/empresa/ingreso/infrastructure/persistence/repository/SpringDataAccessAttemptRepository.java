package com.empresa.ingreso.infrastructure.persistence.repository;

import com.empresa.ingreso.infrastructure.persistence.entity.AccessAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAccessAttemptRepository extends JpaRepository<AccessAttemptEntity, Long> {
}
