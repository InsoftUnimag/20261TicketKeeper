package com.empresa.ingreso.application.service;

import com.empresa.ingreso.application.port.out.LoadModulo1TicketPort;
import com.empresa.ingreso.application.port.out.LoadTicketPort;
import com.empresa.ingreso.application.port.out.SaveTicketPort;
import com.empresa.ingreso.domain.model.Modulo1TicketSnapshot;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.domain.model.TicketStatus;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Modulo1TicketImportService {

    private final LoadTicketPort loadTicketPort;
    private final LoadModulo1TicketPort loadModulo1TicketPort;
    private final SaveTicketPort saveTicketPort;

    public Modulo1TicketImportService(
            LoadTicketPort loadTicketPort,
            LoadModulo1TicketPort loadModulo1TicketPort,
            SaveTicketPort saveTicketPort
    ) {
        this.loadTicketPort = loadTicketPort;
        this.loadModulo1TicketPort = loadModulo1TicketPort;
        this.saveTicketPort = saveTicketPort;
    }

    @Transactional
    public Optional<Ticket> resolveForSession(String scannedTicketId, Long sessionId) {
        Optional<Ticket> localTicket = loadTicketPort.findByCodeForUpdate(scannedTicketId);
        if (localTicket.isPresent()) {
            return localTicket;
        }

        return loadModulo1TicketPort.findById(scannedTicketId)
                .map(snapshot -> saveTicketPort.save(toLocalTicket(snapshot, scannedTicketId, sessionId)));
    }

    private Ticket toLocalTicket(Modulo1TicketSnapshot snapshot, String scannedTicketId, Long sessionId) {
        return new Ticket(
                null,
                scannedTicketId,
                mapStatus(snapshot.status()),
                defaultText(snapshot.category(), "GENERAL"),
                defaultText(snapshot.zone(), "GENERAL"),
                sessionId,
                false,
                snapshot.ticketId(),
                snapshot.eventId(),
                snapshot.seatNumber(),
                snapshot.reEntryAllowed()
        );
    }

    private TicketStatus mapStatus(String status) {
        if (status == null) {
            return TicketStatus.BLOCKED;
        }

        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "VENDIDO", "ACTIVO", "ACTIVE" -> TicketStatus.ACTIVE;
            case "VALIDADO", "ENTERED", "INGRESADO" -> TicketStatus.ENTERED;
            case "SALIO", "EXITED" -> TicketStatus.EXITED;
            case "ANULADO", "CANCELADO", "CANCELED" -> TicketStatus.CANCELED;
            case "REEMBOLSADO", "REFUNDED" -> TicketStatus.REFUNDED;
            case "BLOQUEADO", "BLOCKED" -> TicketStatus.BLOCKED;
            default -> TicketStatus.BLOCKED;
        };
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
