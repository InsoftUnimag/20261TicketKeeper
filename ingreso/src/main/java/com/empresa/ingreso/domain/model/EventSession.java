package com.empresa.ingreso.domain.model;

import java.time.LocalDate;

public record EventSession(
        Long id,
        LocalDate eventDate,
        boolean active,
        Integer maxCapacity,
        Integer currentOccupancy
) {
}
