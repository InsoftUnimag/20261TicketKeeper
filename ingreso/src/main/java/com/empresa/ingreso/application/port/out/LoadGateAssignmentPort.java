package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.GateAssignment;
import java.util.Optional;

public interface LoadGateAssignmentPort { Optional<GateAssignment> findActiveBySessionGateAndCategory(String sessionId, Long gateId, String ticketCategory); boolean existsActiveConflict(String sessionId, Long gateId, String ticketCategory); }
