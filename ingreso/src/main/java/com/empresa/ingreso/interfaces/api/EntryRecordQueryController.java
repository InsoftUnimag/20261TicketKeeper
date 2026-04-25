package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.GetEntryRecordByTicketUseCase;
import com.empresa.ingreso.application.port.in.GetEntryRecordsByEventUseCase;
import com.empresa.ingreso.interfaces.api.dto.EntryRecordResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entry-records")
public class EntryRecordQueryController {
    private final GetEntryRecordByTicketUseCase getEntryRecordByTicketUseCase;
    private final GetEntryRecordsByEventUseCase getEntryRecordsByEventUseCase;

    public EntryRecordQueryController(GetEntryRecordByTicketUseCase getEntryRecordByTicketUseCase, GetEntryRecordsByEventUseCase getEntryRecordsByEventUseCase) {
        this.getEntryRecordByTicketUseCase = getEntryRecordByTicketUseCase; this.getEntryRecordsByEventUseCase = getEntryRecordsByEventUseCase;
    }

    @GetMapping("/tickets/{ticketCode}")
    public ResponseEntity<EntryRecordResponse> byTicket(@PathVariable String ticketCode) {
        var r = getEntryRecordByTicketUseCase.execute(ticketCode);
        return ResponseEntity.ok(new EntryRecordResponse(r.ticketCode(), r.finalStatus(), r.sessionId(), r.gateId(), r.accessType(), r.entryAt()));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<EntryRecordResponse>> byEvent(@PathVariable Long eventId) {
        var result = getEntryRecordsByEventUseCase.execute(eventId).stream()
                .map(r -> new EntryRecordResponse(r.ticketCode(), r.finalStatus(), r.sessionId(), r.gateId(), r.accessType(), r.entryAt()))
                .toList();
        return ResponseEntity.ok(result);
    }
}
