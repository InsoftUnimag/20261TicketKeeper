package com.empresa.ingreso.domain.model;

import java.time.OffsetDateTime;

public record EntryRecord(
        Long ticketId,
        Long eventId,
        Long gateId,
        AccessType accessType,
        OffsetDateTime enteredAt
) {
    public EntryRecord {
        validatePositive(ticketId, "ticketId");
        validatePositive(eventId, "eventId");
        validatePositive(gateId, "gateId");
        requireNonNull(accessType, "accessType");
        requireNonNull(enteredAt, "enteredAt");
    }

    private static void validatePositive(Long value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
