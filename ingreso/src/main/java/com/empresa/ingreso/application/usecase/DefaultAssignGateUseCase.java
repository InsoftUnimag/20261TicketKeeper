package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.AssignGateCommand;
import com.empresa.ingreso.application.port.in.AssignGateResult;
import com.empresa.ingreso.application.port.in.AssignGateUseCase;
import com.empresa.ingreso.application.port.out.LoadEventSessionPort;
import com.empresa.ingreso.application.port.out.LoadGateAssignmentPort;
import com.empresa.ingreso.application.port.out.PublishGateAssignmentPort;
import com.empresa.ingreso.application.port.out.SaveGateAssignmentPort;
import com.empresa.ingreso.domain.model.GateAssignment;
import com.empresa.ingreso.shared.errors.BusinessException;
import com.empresa.ingreso.shared.errors.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultAssignGateUseCase implements AssignGateUseCase {
    private final LoadEventSessionPort loadEventSessionPort;
    private final LoadGateAssignmentPort loadGateAssignmentPort;
    private final SaveGateAssignmentPort saveGateAssignmentPort;
    private final PublishGateAssignmentPort publishGateAssignmentPort;

    public DefaultAssignGateUseCase(LoadEventSessionPort loadEventSessionPort, LoadGateAssignmentPort loadGateAssignmentPort, SaveGateAssignmentPort saveGateAssignmentPort, PublishGateAssignmentPort publishGateAssignmentPort) {
        this.loadEventSessionPort = loadEventSessionPort;
        this.loadGateAssignmentPort = loadGateAssignmentPort;
        this.saveGateAssignmentPort = saveGateAssignmentPort;
        this.publishGateAssignmentPort = publishGateAssignmentPort;
    }

    @Override
    @Transactional
    public AssignGateResult execute(AssignGateCommand command) {
        var session = loadEventSessionPort.findEventSessionById(command.sessionId()).orElseThrow(() -> new BusinessException(ErrorCode.EVENTO_NO_ENCONTRADO, "Evento no encontrado"));
        if (!session.active()) throw new BusinessException(ErrorCode.SESION_INVALIDA, "Sesion inactiva");
        if (loadGateAssignmentPort.existsActiveConflict(command.sessionId(), command.gateId(), command.ticketCategory())) throw new BusinessException(ErrorCode.ASIGNACION_CONFLICTIVA, "La categoria ya esta asignada a esa puerta");
        GateAssignment saved = saveGateAssignmentPort.save(new GateAssignment(null, command.sessionId(), command.gateId(), command.ticketCategory(), command.zone(), true));
        publishGateAssignmentPort.publish(saved);
        return new AssignGateResult("ASSIGNED", "Puerta asignada correctamente", saved.id());
    }
}
