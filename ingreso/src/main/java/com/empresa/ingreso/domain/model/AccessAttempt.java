package com.empresa.ingreso.domain.model;

import com.empresa.ingreso.shared.errors.ErrorCode;
import java.time.OffsetDateTime;

public record AccessAttempt(
        Long id,
        Long ticketId,
        String enteredTicketCode,
        Long readerId,
        Long gateId,
        Long sessionId,
        AccessChannel channel,
        AttemptResult result,
        ErrorCode errorCode,
        OffsetDateTime attemptedAt
) {
}
