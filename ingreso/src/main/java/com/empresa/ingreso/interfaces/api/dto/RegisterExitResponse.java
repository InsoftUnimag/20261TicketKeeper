package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.shared.errors.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterExitResponse", description = "Resultado del registro de salida")
public record RegisterExitResponse(
        @Schema(description = "Estado funcional de la operacion", example = "OK") String status,
        @Schema(description = "Detalle legible de la operacion", example = "Salida registrada") String message,
        @Schema(description = "Codigo de error cuando la operacion falla") ErrorCode errorCode
) {}
