package com.empresa.ingreso.infrastructure.persistence.dto;

import java.time.OffsetDateTime;

public record AttendingTicketInfo(
        String externalTicketId,
        OffsetDateTime firstEntryTime
) {
}
