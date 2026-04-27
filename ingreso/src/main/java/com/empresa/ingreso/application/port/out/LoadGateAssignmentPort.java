package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.GateAssignment;
import java.util.Optional;

public interface LoadGateAssignmentPort { Optional<GateAssignment> findActiveBySessionGateAndCategory(Long sessionId, Long gateId, String ticketCategory); boolean existsActiveConflict(Long sessionId, Long gateId, String ticketCategory); }
