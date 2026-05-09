package com.empresa.ingreso.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AssignGateResponse", description = "Resultado de la asignacion de puerta")
public record AssignGateResponse(
        @Schema(description = "Estado funcional de la operacion", example = "OK") String status,
        @Schema(description = "Detalle legible de la operacion", example = "Asignacion creada") String message,
        @Schema(description = "Identificador de la asignacion creada", example = "10") Long assignmentId
) {}
