package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.GetTicketStatusCommand;
import com.empresa.ingreso.application.port.in.GetTicketStatusUseCase;
import com.empresa.ingreso.interfaces.api.dto.GetTicketStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Consultas del estado operativo de tickets")
public class TicketStatusController {
    private final GetTicketStatusUseCase getTicketStatusUseCase;

    public TicketStatusController(GetTicketStatusUseCase getTicketStatusUseCase) { this.getTicketStatusUseCase = getTicketStatusUseCase; }

    @GetMapping("/{ticketCode}/status")
    @Operation(
            summary = "Consultar estado del ticket",
            description = "Retorna el estado operativo del ticket y el ultimo contexto de acceso conocido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado consultado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetTicketStatusResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "OK",
                                      "message": "Estado consultado",
                                      "errorCode": null,
                                      "ticketStatus": "ENTERED",
                                      "ticketCode": "TK-1001",
                                      "sessionId": 42,
                                      "gateId": 7,
                                      "entryAt": "2026-05-09T10:00:00Z"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<GetTicketStatusResponse> getStatus(
            @Parameter(description = "Codigo unico del ticket") @PathVariable String ticketCode,
            @Parameter(description = "Identificador opcional de auditoria de la consulta") @RequestHeader(value = "X-Requested-By", required = false) String requestedBy) {
        var result = getTicketStatusUseCase.execute(new GetTicketStatusCommand(ticketCode, requestedBy));
        return ResponseEntity.ok(new GetTicketStatusResponse(result.status(), result.message(), result.errorCode(), result.ticketStatus(), result.ticketCode(), result.sessionId(), result.gateId(), result.entryAt()));
    }
}
