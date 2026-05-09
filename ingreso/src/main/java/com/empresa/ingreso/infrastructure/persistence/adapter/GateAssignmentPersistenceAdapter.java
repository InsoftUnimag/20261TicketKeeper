package com.empresa.ingreso.infrastructure.persistence.adapter;

import com.empresa.ingreso.application.port.out.LoadGateAssignmentPort;
import com.empresa.ingreso.application.port.out.SaveGateAssignmentPort;
import com.empresa.ingreso.domain.model.GateAssignment;
import com.empresa.ingreso.infrastructure.persistence.entity.GateAssignmentEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataGateAssignmentRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class GateAssignmentPersistenceAdapter implements
        LoadGateAssignmentPort,
        SaveGateAssignmentPort {

    private final SpringDataGateAssignmentRepository gateAssignmentRepository;

    public GateAssignmentPersistenceAdapter(SpringDataGateAssignmentRepository gateAssignmentRepository) {
        this.gateAssignmentRepository = gateAssignmentRepository;
    }

    @Override
    public Optional<GateAssignment> findActiveBySessionGateAndCategory(Long sessionId, Long gateId, String ticketCategory) {
        return gateAssignmentRepository
                .findFirstBySessionIdAndGateIdAndTicketCategoryAndActiveTrue(sessionId, gateId, ticketCategory)
                .map(this::toDomain);
    }

    @Override
    public boolean existsActiveConflict(Long sessionId, Long gateId, String ticketCategory) {
        return gateAssignmentRepository.existsBySessionIdAndGateIdAndTicketCategoryAndActiveTrue(
                sessionId,
                gateId,
                ticketCategory
        );
    }

    @Override
    public GateAssignment save(GateAssignment assignment) {
        GateAssignmentEntity entity = new GateAssignmentEntity();
        entity.setId(assignment.id());
        entity.setSessionId(assignment.sessionId());
        entity.setGateId(assignment.gateId());
        entity.setTicketCategory(assignment.ticketCategory());
        entity.setZone(assignment.zone());
        entity.setActive(assignment.active());
        return toDomain(gateAssignmentRepository.save(entity));
    }

    private GateAssignment toDomain(GateAssignmentEntity entity) {
        return new GateAssignment(
                entity.getId(),
                entity.getSessionId(),
                entity.getGateId(),
                entity.getTicketCategory(),
                entity.getZone(),
                entity.isActive()
        );
    }
}
