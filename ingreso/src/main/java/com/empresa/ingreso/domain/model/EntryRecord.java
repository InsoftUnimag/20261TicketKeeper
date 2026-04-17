package com.empresa.ingreso.domain.model;

import java.time.OffsetDateTime;

public record EntryRecord(
        Long ticketId,
        Long eventId,
        Long gateId,
        AccessType accessType,
        OffsetDateTime enteredAt
) {
}
