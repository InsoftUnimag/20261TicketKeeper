package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.GetTicketStatusCommand;
import com.empresa.ingreso.application.port.in.GetTicketStatusUseCase;
import com.empresa.ingreso.interfaces.api.dto.GetTicketStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketStatusController {
    private final GetTicketStatusUseCase getTicketStatusUseCase;

    public TicketStatusController(GetTicketStatusUseCase getTicketStatusUseCase) { this.getTicketStatusUseCase = getTicketStatusUseCase; }

    @GetMapping("/{ticketCode}/status")
    public ResponseEntity<GetTicketStatusResponse> getStatus(@PathVariable String ticketCode, @RequestHeader(value = "X-Requested-By", required = false) String requestedBy) {
        var result = getTicketStatusUseCase.execute(new GetTicketStatusCommand(ticketCode, requestedBy));
        return ResponseEntity.ok(new GetTicketStatusResponse(result.status(), result.message(), result.errorCode(), result.ticketStatus(), result.ticketCode(), result.sessionId(), result.gateId(), result.entryAt()));
    }
}
