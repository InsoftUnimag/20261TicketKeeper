package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.ProcessEntryAttemptCommand;
import com.empresa.ingreso.application.port.in.ProcessEntryAttemptResult;
import com.empresa.ingreso.application.port.in.ProcessEntryAttemptUseCase;
import com.empresa.ingreso.application.service.Modulo1TicketImportService;
import com.empresa.ingreso.infrastructure.integration.adapter.Modulo1TicketRestAdapter;
import com.empresa.ingreso.interfaces.api.dto.ProcessEntryAttemptRequest;
import com.empresa.ingreso.interfaces.api.dto.ProcessEntryAttemptResponse;
import com.empresa.ingreso.shared.errors.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
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
    private final Modulo1TicketRestAdapter modulo1TicketRestAdapter;
    private final Modulo1TicketImportService modulo1TicketImportService;

    public EntryController(
            ProcessEntryAttemptUseCase processEntryAttemptUseCase,
            Modulo1TicketRestAdapter modulo1TicketRestAdapter,
            Modulo1TicketImportService modulo1TicketImportService
    ) {
        this.processEntryAttemptUseCase = processEntryAttemptUseCase;
        this.modulo1TicketRestAdapter = modulo1TicketRestAdapter;
        this.modulo1TicketImportService = modulo1TicketImportService;
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
        ProcessEntryAttemptCommand command = new ProcessEntryAttemptCommand(
                request.ticketCode(),
                request.readerId(),
                request.gateId(),
                request.sessionId(),
                request.channel()
        );
        Optional<ProcessEntryAttemptResult> result = processEntryAttemptUseCase.execute(command);
        if (result.isEmpty()) {
            result = resolveFromModulo1(command);
        }

        if (result.isEmpty()) {
            ProcessEntryAttemptResponse notFoundResponse = new ProcessEntryAttemptResponse(
                    "REJECTED",
                    "Ticket was not found",
                    ErrorCode.TICKET_NO_ENCONTRADO,
                    null
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(notFoundResponse);
        }

        ProcessEntryAttemptResponse response = new ProcessEntryAttemptResponse(
                result.get().status(),
                result.get().message(),
                result.get().errorCode(),
                result.get().attemptId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Optional<ProcessEntryAttemptResult> resolveFromModulo1(ProcessEntryAttemptCommand command) {
        if (modulo1TicketRestAdapter.findById(command.ticketCode()).isEmpty()) {
            return Optional.empty();
        }

        modulo1TicketImportService.resolveForSession(command.ticketCode(), command.sessionId());
        return processEntryAttemptUseCase.execute(command);
    }
}
