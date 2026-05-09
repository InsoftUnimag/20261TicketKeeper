package com.empresa.ingreso.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AssignGateRequest", description = "Solicitud para asignar una puerta a una sesion y categoria de ticket")
public record AssignGateRequest(
        @Schema(description = "Identificador de la sesion", example = "42") @NotNull Long sessionId,
        @Schema(description = "Identificador de la puerta", example = "7") @NotNull Long gateId,
        @Schema(description = "Categoria del ticket habilitada para la puerta", example = "GENERAL") @NotBlank String ticketCategory,
        @Schema(description = "Zona fisica asociada a la puerta", example = "NORTE") @NotBlank String zone
) {}
