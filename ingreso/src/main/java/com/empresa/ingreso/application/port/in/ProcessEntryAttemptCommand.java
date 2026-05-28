package com.empresa.ingreso.application.port.in;

import com.empresa.ingreso.domain.model.AccessChannel;

public record ProcessEntryAttemptCommand(
        String ticketCode,
        Long readerId,
        Long gateId,
        String sessionId,
        String assignedZone, // <-- Campo añadido
        AccessChannel channel
) {
}
