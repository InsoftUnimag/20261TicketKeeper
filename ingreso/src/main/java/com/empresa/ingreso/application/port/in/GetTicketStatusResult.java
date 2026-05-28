package com.empresa.ingreso.application.port.in;

import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.shared.errors.ErrorCode;
import java.time.OffsetDateTime;

public record GetTicketStatusResult(
    String status,
    String message,
    ErrorCode errorCode,
    TicketStatus ticketStatus,
    String ticketCode,
    String eventId,
    String eventName,
    String recintoName,
    String zoneName,
    Long gateId,
    OffsetDateTime entryAt
) {}
