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

        if (used && status != TicketStatus.ENTERED && status != TicketStatus.EXITED) {
            throw new IllegalArgumentException("A used ticket must have ENTERED or EXITED status");
        }

        if (!used && (status == TicketStatus.ENTERED || status == TicketStatus.EXITED)) {
            throw new IllegalArgumentException("An entered or exited ticket must be marked as used");
        }
    }

    public boolean isInvalidForAccess() {
        return status == TicketStatus.CANCELED || status == TicketStatus.REFUNDED || status == TicketStatus.BLOCKED;
    }

    public boolean canRegisterFirstEntry() {
        return status == TicketStatus.ACTIVE && !used;
    }

    public boolean canRegisterExit() {
        return status == TicketStatus.ENTERED && used;
    }

    public boolean canRegisterReEntry() {
        return status == TicketStatus.EXITED && used;
    }

    public Ticket markEntered() {
        return new Ticket(id, code, TicketStatus.ENTERED, category, allowedZone, sessionId, true);
    }

    public Ticket markExited() {
        return new Ticket(id, code, TicketStatus.EXITED, category, allowedZone, sessionId, true);
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
