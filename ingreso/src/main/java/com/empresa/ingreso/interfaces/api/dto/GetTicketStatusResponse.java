package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.shared.errors.ErrorCode;
import java.time.OffsetDateTime;

public record GetTicketStatusResponse(String status, String message, ErrorCode errorCode, TicketStatus ticketStatus, String ticketCode, Long sessionId, Long gateId, OffsetDateTime entryAt) {}
