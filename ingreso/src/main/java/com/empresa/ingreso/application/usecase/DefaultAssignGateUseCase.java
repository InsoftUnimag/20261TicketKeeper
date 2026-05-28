package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.AssignGateCommand;
import com.empresa.ingreso.application.port.in.AssignGateResult;
import com.empresa.ingreso.application.port.in.AssignGateUseCase;
import com.empresa.ingreso.application.port.out.LoadGateAssignmentPort;
import com.empresa.ingreso.application.port.out.Modulo1Port;
import com.empresa.ingreso.application.port.out.PublishGateAssignmentPort;
import com.empresa.ingreso.application.port.out.SaveGateAssignmentPort;
import com.empresa.ingreso.domain.model.GateAssignment;
import com.empresa.ingreso.shared.errors.BusinessException;
import com.empresa.ingreso.shared.errors.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultAssignGateUseCase implements AssignGateUseCase {
    private final Modulo1Port modulo1Port;
    private final LoadGateAssignmentPort loadGateAssignmentPort;
    private final SaveGateAssignmentPort saveGateAssignmentPort;
    private final PublishGateAssignmentPort publishGateAssignmentPort;

    public DefaultAssignGateUseCase(Modulo1Port modulo1Port, LoadGateAssignmentPort loadGateAssignmentPort, SaveGateAssignmentPort saveGateAssignmentPort, PublishGateAssignmentPort publishGateAssignmentPort) {
        this.modulo1Port = modulo1Port;
        this.loadGateAssignmentPort = loadGateAssignmentPort;
        this.saveGateAssignmentPort = saveGateAssignmentPort;
        this.publishGateAssignmentPort = publishGateAssignmentPort;
    }

    @Override
    @Transactional
    public AssignGateResult execute(AssignGateCommand command) {
        // Validar que el evento existe y está activo en el Módulo 1
        modulo1Port.findActiveEventById(command.sessionId()).orElseThrow(() -> new BusinessException(ErrorCode.EVENTO_NO_ENCONTRADO, "Evento no encontrado o inactivo en Módulo 1"));

        // El resto de la lógica no cambia, pero ahora usa el sessionId como String
        if (loadGateAssignmentPort.existsActiveConflict(command.sessionId(), command.gateId(), command.ticketCategory())) {
            throw new BusinessException(ErrorCode.ASIGNACION_CONFLICTIVA, "La categoria ya esta asignada a esa puerta");
        }
        GateAssignment saved = saveGateAssignmentPort.save(new GateAssignment(null, command.sessionId(), command.gateId(), command.ticketCategory(), command.zone(), true));
        publishGateAssignmentPort.publish(saved);
        return new AssignGateResult("ASSIGNED", "Puerta asignada correctamente", saved.id());
    }
}
