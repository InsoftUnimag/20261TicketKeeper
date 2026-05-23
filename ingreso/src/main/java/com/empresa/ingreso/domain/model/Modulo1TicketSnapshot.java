package com.empresa.ingreso.domain.model;

import java.time.LocalDateTime;

public record  Modulo1TicketSnapshot(
        String ticketId,
        String eventId,
        String status,
        String category,
        String zone,
        String assignedGate,
        LocalDateTime eventDate,
        String seatNumber,
        boolean reEntryAllowed
) {
}
