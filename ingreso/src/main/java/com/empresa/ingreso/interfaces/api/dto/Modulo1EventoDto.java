package com.empresa.ingreso.interfaces.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Modulo1EventoDto(
        String id,
        String nombre,
        String recintoId,
        String estado
) {
}
