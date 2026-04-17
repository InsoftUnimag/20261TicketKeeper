package com.empresa.ingreso.domain.model;

public record ReaderDevice(
        Long id,
        Long gateId,
        String assignedZone,
        boolean enabled
) {
}
