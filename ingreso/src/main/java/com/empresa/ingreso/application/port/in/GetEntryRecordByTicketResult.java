package com.empresa.ingreso.application.port.in;

import com.empresa.ingreso.domain.model.AccessType;
import java.time.OffsetDateTime;

public record GetEntryRecordByTicketResult(String ticketCode, String finalStatus, Long sessionId, Long gateId, AccessType accessType, OffsetDateTime entryAt) {}
