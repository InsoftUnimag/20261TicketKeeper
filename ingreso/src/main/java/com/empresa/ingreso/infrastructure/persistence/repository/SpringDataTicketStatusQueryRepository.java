package com.empresa.ingreso.infrastructure.persistence.repository;

import com.empresa.ingreso.infrastructure.persistence.entity.TicketStatusQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTicketStatusQueryRepository extends JpaRepository<TicketStatusQueryEntity, Long> {
}
