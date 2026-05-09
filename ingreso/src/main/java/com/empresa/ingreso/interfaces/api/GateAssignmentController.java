package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.AssignGateCommand;
import com.empresa.ingreso.application.port.in.AssignGateUseCase;
import com.empresa.ingreso.interfaces.api.dto.AssignGateRequest;
import com.empresa.ingreso.interfaces.api.dto.AssignGateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gate-assignments")
@Tag(name = "Configuracion", description = "Configuracion de asignaciones entre sesiones, puertas y categorias")
public class GateAssignmentController {
    private final AssignGateUseCase assignGateUseCase;
    public GateAssignmentController(AssignGateUseCase assignGateUseCase) { this.assignGateUseCase = assignGateUseCase; }

    @PostMapping
    @Operation(
            summary = "Asignar puerta a una sesion",
            description = "Registra la configuracion operativa que vincula una puerta con una sesion, categoria de ticket y zona."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Asignacion creada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AssignGateResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "OK",
                                      "message": "Asignacion creada",
                                      "assignmentId": 10
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @ApiResponse(responseCode = "409", description = "Asignacion conflictiva")
    })
    public ResponseEntity<AssignGateResponse> assign(@Valid @RequestBody AssignGateRequest request) {
        var result = assignGateUseCase.execute(new AssignGateCommand(request.sessionId(), request.gateId(), request.ticketCategory(), request.zone()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AssignGateResponse(result.status(), result.message(), result.assignmentId()));
    }
}
