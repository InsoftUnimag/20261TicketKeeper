package com.empresa.ingreso.infrastructure.publication;

import com.empresa.ingreso.application.port.out.PublishGateAssignmentPort;
import com.empresa.ingreso.domain.model.GateAssignment;
import org.springframework.stereotype.Component;

@Component
public class NoOpGateAssignmentPublisherAdapter implements PublishGateAssignmentPort {

    @Override
    public void publish(GateAssignment assignment) {
        // No-op: external publication is not implemented yet.
    }
}
