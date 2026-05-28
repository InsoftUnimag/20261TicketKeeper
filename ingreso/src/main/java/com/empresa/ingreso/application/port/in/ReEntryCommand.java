package com.empresa.ingreso.application.port.in;

import com.empresa.ingreso.domain.model.AccessChannel;

public record ReEntryCommand(String ticketCode, Long readerId, Long gateId, String sessionId, AccessChannel channel) {}
