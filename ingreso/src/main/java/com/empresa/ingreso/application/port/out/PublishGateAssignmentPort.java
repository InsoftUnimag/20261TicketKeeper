package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.GateAssignment;

public interface PublishGateAssignmentPort { void publish(GateAssignment assignment); }
