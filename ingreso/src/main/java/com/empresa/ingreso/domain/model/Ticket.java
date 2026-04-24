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
    public Ticket {
        validatePositive(id, "id");
        requireText(code, "code");
        requireNonNull(status, "status");
        requireText(category, "category");
        requireText(allowedZone, "allowedZone");
        validatePositive(sessionId, "sessionId");

        if (used && status != TicketStatus.ENTERED) {
            throw new IllegalArgumentException("A used ticket must have ENTERED status");
        }

        if (!used && status == TicketStatus.ENTERED) {
            throw new IllegalArgumentException("An ENTERED ticket must be marked as used");
        }
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

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
