package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.AssignGateCommand;
import com.empresa.ingreso.application.port.in.AssignGateUseCase;
import com.empresa.ingreso.interfaces.api.dto.AssignGateRequest;
import com.empresa.ingreso.interfaces.api.dto.AssignGateResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gate-assignments")
public class GateAssignmentController {
    private final AssignGateUseCase assignGateUseCase;
    public GateAssignmentController(AssignGateUseCase assignGateUseCase) { this.assignGateUseCase = assignGateUseCase; }

    @PostMapping
    public ResponseEntity<AssignGateResponse> assign(@Valid @RequestBody AssignGateRequest request) {
        var result = assignGateUseCase.execute(new AssignGateCommand(request.sessionId(), request.gateId(), request.ticketCategory(), request.zone()));
        return ResponseEntity.ok(new AssignGateResponse(result.status(), result.message(), result.assignmentId()));
    }
}
