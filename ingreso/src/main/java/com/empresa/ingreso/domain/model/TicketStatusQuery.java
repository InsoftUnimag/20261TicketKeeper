package com.empresa.ingreso.domain.model;

import java.time.OffsetDateTime;

public record TicketStatusQuery(
        Long id,
        String ticketCode,
        OffsetDateTime queriedAt,
        String requestedBy
) {
    public TicketStatusQuery {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be greater than zero");
        requireText(ticketCode, "ticketCode");
        if (queriedAt == null) throw new IllegalArgumentException("queriedAt is required");
        if (requestedBy != null && requestedBy.isBlank()) throw new IllegalArgumentException("requestedBy cannot be blank");
    }
    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(fieldName + " is required");
    }
}
