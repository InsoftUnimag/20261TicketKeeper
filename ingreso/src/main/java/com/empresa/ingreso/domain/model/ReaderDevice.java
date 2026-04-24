package com.empresa.ingreso.domain.model;

public record ReaderDevice(
        Long id,
        Long gateId,
        String assignedZone,
        boolean enabled
) {
    public ReaderDevice {
        validatePositive(id, "id");
        validatePositive(gateId, "gateId");
        requireText(assignedZone, "assignedZone");
    }

    private static void validatePositive(Long value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
