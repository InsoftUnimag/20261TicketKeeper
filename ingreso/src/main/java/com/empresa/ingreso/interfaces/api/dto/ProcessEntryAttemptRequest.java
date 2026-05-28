package com.empresa.ingreso.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ProcessEntryAttemptRequest", description = "Solicitud para validar y registrar un intento de ingreso")
public record ProcessEntryAttemptRequest(
        @Schema(description = "Codigo unico del ticket", example = "TK-1001") @NotBlank String ticketCode
) {
}
