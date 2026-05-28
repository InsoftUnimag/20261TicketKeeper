package com.empresa.ingreso.infrastructure.persistence.repository;

import com.empresa.ingreso.infrastructure.persistence.entity.GateAssignmentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataGateAssignmentRepository extends JpaRepository<GateAssignmentEntity, Long> {
    Optional<GateAssignmentEntity> findFirstBySessionIdAndGateIdAndTicketCategoryAndActiveTrue(String sessionId, Long gateId, String ticketCategory);
    boolean existsBySessionIdAndGateIdAndTicketCategoryAndActiveTrue(String sessionId, Long gateId, String ticketCategory);
}
