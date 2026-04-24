package com.empresa.ingreso.domain.model;

import java.time.LocalDate;

public record EventSession(
        Long id,
        LocalDate eventDate,
        boolean active,
        Integer maxCapacity,
        Integer currentOccupancy
) {
    public EventSession {
        validatePositive(id, "id");
        requireNonNull(eventDate, "eventDate");

        if (maxCapacity != null && maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be greater than zero");
        }

        if (currentOccupancy != null && currentOccupancy < 0) {
            throw new IllegalArgumentException("currentOccupancy cannot be negative");
        }

        if (maxCapacity == null && currentOccupancy != null) {
            throw new IllegalArgumentException("currentOccupancy requires a maxCapacity");
        }

        if (maxCapacity != null && currentOccupancy != null && currentOccupancy > maxCapacity) {
            throw new IllegalArgumentException("currentOccupancy cannot exceed maxCapacity");
        }
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
}
