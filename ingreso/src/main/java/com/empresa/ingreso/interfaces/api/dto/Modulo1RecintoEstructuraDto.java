package com.empresa.ingreso.interfaces.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Modulo1RecintoEstructuraDto(
        String recintoId,
        List<ZonaDto> zonas
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ZonaDto(String nombre) {}
}
