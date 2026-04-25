package com.empresa.ingreso.domain.model;

public record GateAssignment(
        Long id,
        Long sessionId,
        Long gateId,
        String ticketCategory,
        String zone,
        boolean active
) {
    public GateAssignment {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be greater than zero");
        validatePositive(sessionId, "sessionId");
        validatePositive(gateId, "gateId");
        requireText(ticketCategory, "ticketCategory");
        requireText(zone, "zone");
    }

    public boolean matches(Ticket ticket, Long requestedGateId) {
        return active
                && sessionId.equals(ticket.sessionId())
                && gateId.equals(requestedGateId)
                && ticketCategory.equals(ticket.category())
                && zone.equals(ticket.allowedZone());
    }

    private static void validatePositive(Long value, String fieldName) {
        if (value == null || value <= 0) throw new IllegalArgumentException(fieldName + " must be greater than zero");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(fieldName + " is required");
    }
}
