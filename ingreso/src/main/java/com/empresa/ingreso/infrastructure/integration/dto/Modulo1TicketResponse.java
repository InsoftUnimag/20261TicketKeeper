package com.empresa.ingreso.infrastructure.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Modulo1TicketResponse(
        String ticketId,
        String eventoId,
        String estado,
        String categoria,
        String zona,
        String compuertaAsignada,
        LocalDateTime fechaEvento,
        String numeroAsiento,
        boolean permiteReingreso
) {
}
