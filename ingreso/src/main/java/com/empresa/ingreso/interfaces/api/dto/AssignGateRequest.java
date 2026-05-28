package com.empresa.ingreso.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AssignGateRequest", description = "Solicitud para asignar una puerta a una sesion y categoria de ticket")
public record AssignGateRequest(
        @Schema(description = "Identificador del evento (UUID)", example = "00016ec5-90fb-4c63-aba9-3ea17abd27c0") @NotBlank String sessionId,
        @Schema(description = "Identificador de la puerta", example = "7") @NotNull Long gateId,
        @Schema(description = "Categoria del ticket habilitada para la puerta", example = "GENERAL") @NotBlank String ticketCategory,
        @Schema(description = "Zona fisica asociada a la puerta", example = "NORTE") @NotBlank String zone
) {}
