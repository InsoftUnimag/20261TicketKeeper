package com.empresa.ingreso.domain.model;

import com.empresa.ingreso.shared.errors.ErrorCode;
import java.time.OffsetDateTime;

public record AccessAttempt(
        Long id,
        Long ticketId,
        String enteredTicketCode,
        Long readerId,
        Long gateId,
        String sessionId,
        AccessChannel channel,
        AttemptResult result,
        ErrorCode errorCode,
        OffsetDateTime attemptedAt
) {
    public AccessAttempt {
        validatePositive(readerId, "readerId");
        validatePositive(gateId, "gateId");
        requireText(sessionId, "sessionId");
        requireText(enteredTicketCode, "enteredTicketCode");
        requireNonNull(channel, "channel");
        requireNonNull(result, "result");
        requireNonNull(attemptedAt, "attemptedAt");

        if (id != null) {
            validatePositive(id, "id");
        }

        if (ticketId != null) {
            validatePositive(ticketId, "ticketId");
        }

        if (result == AttemptResult.APPROVED && errorCode != null) {
            throw new IllegalArgumentException("Approved attempts cannot contain an errorCode");
        }

        if (result == AttemptResult.REJECTED && errorCode == null) {
            throw new IllegalArgumentException("Rejected attempts must contain an errorCode");
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