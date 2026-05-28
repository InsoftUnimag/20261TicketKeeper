package com.empresa.ingreso.interfaces.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttendanceSummaryResponse(
        @JsonProperty("eventoId") String eventId,
        @JsonProperty("totalTicketsValidados") long totalTicketsValidated
) {
}
