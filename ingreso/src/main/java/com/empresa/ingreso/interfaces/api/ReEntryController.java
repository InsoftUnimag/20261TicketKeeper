package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.ReEntryCommand;
import com.empresa.ingreso.application.port.in.ReEntryUseCase;
import com.empresa.ingreso.application.port.in.RegisterExitCommand;
import com.empresa.ingreso.application.port.in.RegisterExitUseCase;
import com.empresa.ingreso.interfaces.api.dto.ReEntryRequest;
import com.empresa.ingreso.interfaces.api.dto.ReEntryResponse;
import com.empresa.ingreso.interfaces.api.dto.RegisterExitRequest;
import com.empresa.ingreso.interfaces.api.dto.RegisterExitResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/access-flow")
@Tag(name = "Flujo de acceso", description = "Operaciones de salida y reingreso de tickets")
public class ReEntryController {
    private final ReEntryUseCase reEntryUseCase;
    private final RegisterExitUseCase registerExitUseCase;

    public ReEntryController(ReEntryUseCase reEntryUseCase, RegisterExitUseCase registerExitUseCase) {
        this.reEntryUseCase = reEntryUseCase; this.registerExitUseCase = registerExitUseCase;
    }

    @PostMapping("/re-entries")
    @Operation(
            summary = "Registrar reingreso",
            description = "Valida si el ticket puede volver a entrar despues de una salida previa y registra el evento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reingreso procesado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReEntryResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "OK",
                                      "message": "Reingreso permitido",
                                      "errorCode": null,
                                      "reEntriesUsed": 1,
                                      "reEntryLimit": 2
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Reingreso no permitido o solicitud invalida")
    })
    public ResponseEntity<ReEntryResponse> reEntry(@Valid @RequestBody ReEntryRequest request) {
        var result = reEntryUseCase.execute(new ReEntryCommand(request.ticketCode(), request.readerId(), request.gateId(), request.sessionId(), request.channel()));
        return ResponseEntity.ok(new ReEntryResponse(result.status(), result.message(), result.errorCode(), result.reEntriesUsed(), result.reEntryLimit()));
    }

    @PostMapping("/exits")
    @Operation(
            summary = "Registrar salida",
            description = "Registra la salida de un ticket para habilitar controles posteriores del flujo de acceso."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Salida procesada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterExitResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "OK",
                                      "message": "Salida registrada",
                                      "errorCode": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Salida no permitida o solicitud invalida")
    })
    public ResponseEntity<RegisterExitResponse> exit(@Valid @RequestBody RegisterExitRequest request) {
        var result = registerExitUseCase.execute(new RegisterExitCommand(request.ticketCode(), request.readerId(), request.gateId(), request.sessionId(), request.channel()));
        return ResponseEntity.ok(new RegisterExitResponse(result.status(), result.message(), result.errorCode()));
    }
}
