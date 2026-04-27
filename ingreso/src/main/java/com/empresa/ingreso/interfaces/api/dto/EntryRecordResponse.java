package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.domain.model.AccessType;
import java.time.OffsetDateTime;

public record EntryRecordResponse(String ticketCode, String finalStatus, Long sessionId, Long gateId, AccessType accessType, OffsetDateTime entryAt) {}
