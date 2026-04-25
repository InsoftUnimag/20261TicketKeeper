package com.empresa.ingreso.domain.model;

public record Ticket(
        Long id,
        String code,
        TicketStatus status,
        String category,
        String allowedZone,
        Long sessionId,
        boolean used
) {
}
