package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.ProcessEntryAttemptCommand;
import com.empresa.ingreso.application.port.in.ProcessEntryAttemptResult;
import com.empresa.ingreso.application.port.in.ProcessEntryAttemptUseCase;
import com.empresa.ingreso.interfaces.api.dto.ProcessEntryAttemptRequest;
import com.empresa.ingreso.interfaces.api.dto.ProcessEntryAttemptResponse;
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
@RequestMapping("/api/v1/entry-attempts")
@Tag(name = "Ingreso", description = "Operaciones para validar intentos de ingreso")
public class EntryController {

    private final ProcessEntryAttemptUseCase processEntryAttemptUseCase;

    public EntryController(ProcessEntryAttemptUseCase processEntryAttemptUseCase) {
        this.processEntryAttemptUseCase = processEntryAttemptUseCase;
    }

    @PostMapping
    @Operation(
            summary = "Procesar intento de ingreso",
            description = "Valida si un ticket puede ingresar a una sesion por una puerta determinada y registra el intento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Intento de ingreso procesado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProcessEntryAttemptResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "OK",
                                      "message": "Ingreso permitido",
                                      "errorCode": null,
                                      "attemptId": 125
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud invalida o reglas de negocio incumplidas",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "ERROR",
                                      "message": "El ticket ya fue utilizado",
                                      "errorCode": "TICKET_DUPLICADO"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ProcessEntryAttemptResponse> process(@Valid @RequestBody ProcessEntryAttemptRequest request) {
        ProcessEntryAttemptResult result = processEntryAttemptUseCase.execute(new ProcessEntryAttemptCommand(
                request.ticketCode(),
                request.readerId(),
                request.gateId(),
                request.sessionId(),
                request.channel()
        ));
        ProcessEntryAttemptResponse response = new ProcessEntryAttemptResponse(
                result.status(),
                result.message(),
                result.errorCode(),
                result.attemptId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
