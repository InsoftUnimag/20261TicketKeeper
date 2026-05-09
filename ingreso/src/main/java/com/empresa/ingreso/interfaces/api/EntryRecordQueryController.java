package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.GetEntryRecordByTicketUseCase;
import com.empresa.ingreso.application.port.in.GetEntryRecordsByEventUseCase;
import com.empresa.ingreso.interfaces.api.dto.EntryRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entry-records")
@Tag(name = "Consultas", description = "Consultas de registros de ingreso por ticket y evento")
public class EntryRecordQueryController {
    private final GetEntryRecordByTicketUseCase getEntryRecordByTicketUseCase;
    private final GetEntryRecordsByEventUseCase getEntryRecordsByEventUseCase;

    public EntryRecordQueryController(GetEntryRecordByTicketUseCase getEntryRecordByTicketUseCase, GetEntryRecordsByEventUseCase getEntryRecordsByEventUseCase) {
        this.getEntryRecordByTicketUseCase = getEntryRecordByTicketUseCase; this.getEntryRecordsByEventUseCase = getEntryRecordsByEventUseCase;
    }

    @GetMapping("/tickets/{ticketCode}")
    @Operation(
            summary = "Consultar registro por ticket",
            description = "Obtiene el ultimo estado conocido de acceso asociado a un ticket."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EntryRecordResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "ticketCode": "TK-1001",
                                      "finalStatus": "ENTERED",
                                      "sessionId": 42,
                                      "gateId": 7,
                                      "accessType": "ENTRY",
                                      "entryAt": "2026-05-09T10:00:00Z"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Ticket sin registro")
    })
    public ResponseEntity<EntryRecordResponse> byTicket(@Parameter(description = "Codigo unico del ticket") @PathVariable String ticketCode) {
        var r = getEntryRecordByTicketUseCase.execute(ticketCode);
        return ResponseEntity.ok(new EntryRecordResponse(r.ticketCode(), r.finalStatus(), r.sessionId(), r.gateId(), r.accessType(), r.entryAt()));
    }

    @GetMapping("/events/{eventId}")
    @Operation(
            summary = "Listar registros por evento",
            description = "Retorna los registros de acceso asociados a un evento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registros encontrados",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EntryRecordResponse.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "ticketCode": "TK-1001",
                                        "finalStatus": "ENTERED",
                                        "sessionId": 42,
                                        "gateId": 7,
                                        "accessType": "ENTRY",
                                        "entryAt": "2026-05-09T10:00:00Z"
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<List<EntryRecordResponse>> byEvent(@Parameter(description = "Identificador numerico del evento") @PathVariable Long eventId) {
        var result = getEntryRecordsByEventUseCase.execute(eventId).stream()
                .map(r -> new EntryRecordResponse(r.ticketCode(), r.finalStatus(), r.sessionId(), r.gateId(), r.accessType(), r.entryAt()))
                .toList();
        return ResponseEntity.ok(result);
    }
}
