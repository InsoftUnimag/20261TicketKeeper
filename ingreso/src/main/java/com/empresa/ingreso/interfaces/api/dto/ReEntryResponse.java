package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.shared.errors.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ReEntryResponse", description = "Resultado del procesamiento de un reingreso")
public record ReEntryResponse(
        @Schema(description = "Estado funcional de la operacion", example = "OK") String status,
        @Schema(description = "Detalle legible de la operacion", example = "Reingreso permitido") String message,
        @Schema(description = "Codigo de error cuando el reingreso falla") ErrorCode errorCode,
        @Schema(description = "Cantidad de reingresos ya utilizados", example = "1") Integer reEntriesUsed,
        @Schema(description = "Limite total de reingresos permitidos", example = "2") Integer reEntryLimit
) {}
