package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.shared.errors.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "GetTicketStatusResponse", description = "Estado operativo de un ticket")
public record GetTicketStatusResponse(
        @Schema(description = "Estado funcional de la respuesta", example = "OK") String status,
        @Schema(description = "Detalle legible del resultado", example = "Estado consultado") String message,
        @Schema(description = "Codigo de error cuando la operacion falla") ErrorCode errorCode,
        @Schema(description = "Estado actual del ticket") TicketStatus ticketStatus,
        @Schema(description = "Codigo del ticket", example = "TK-1001") String ticketCode,
        @Schema(description = "Sesion asociada al ticket", example = "42") Long sessionId,
        @Schema(description = "Ultima puerta relacionada con el ticket", example = "7") Long gateId,
        @Schema(description = "Fecha y hora del ultimo movimiento", example = "2026-05-09T10:00:00Z") OffsetDateTime entryAt
) {}
