package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.shared.errors.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProcessEntryAttemptResponse", description = "Resultado del procesamiento de un intento de ingreso")
public record ProcessEntryAttemptResponse(
        @Schema(description = "Estado funcional de la operacion", example = "OK") String status,
        @Schema(description = "Detalle legible de la operacion", example = "Ingreso permitido") String message,
        @Schema(description = "Codigo de error cuando la operacion falla") ErrorCode errorCode,
        @Schema(description = "Identificador del intento registrado", example = "125") Long attemptId
) {
}
