package com.empresa.ingreso.application.port.in;

import com.empresa.ingreso.domain.model.AccessChannel;

public record RegisterExitCommand(String ticketCode, Long readerId, Long gateId, Long sessionId, AccessChannel channel) {}
