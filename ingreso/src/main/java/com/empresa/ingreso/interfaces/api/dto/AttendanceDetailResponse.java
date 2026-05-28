package com.empresa.ingreso.interfaces.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record AttendanceDetailResponse(
        @JsonProperty("eventoId") String eventId,
        @JsonProperty("tickets") List<AttendingTicketDto> tickets
) {
    public record AttendingTicketDto(
            @JsonProperty("ticketId") String ticketId,
            @JsonProperty("estadoIngreso") String status,
            @JsonProperty("fechaHoraIngreso") OffsetDateTime entryTimestamp
    ) {
    }
}
