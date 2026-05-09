package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.domain.model.AccessChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "ReEntryRequest", description = "Solicitud para registrar un reingreso")
public record ReEntryRequest(
        @Schema(description = "Codigo unico del ticket", example = "TK-1001") @NotBlank String ticketCode,
        @Schema(description = "Identificador del lector", example = "15") @NotNull Long readerId,
        @Schema(description = "Identificador de la puerta", example = "7") @NotNull Long gateId,
        @Schema(description = "Identificador de la sesion", example = "42") @NotNull Long sessionId,
        @Schema(description = "Canal de captura del acceso", allowableValues = {"QR", "MANUAL"}, example = "QR") @NotNull AccessChannel channel
) {}
