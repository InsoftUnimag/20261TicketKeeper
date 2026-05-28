package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.domain.model.AccessType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "EntryRecordResponse", description = "Registro consolidado de acceso asociado a un ticket")
public record EntryRecordResponse(
        @Schema(description = "Codigo del ticket", example = "TK-1001") String ticketCode,
        @Schema(description = "Estado final registrado para el ticket", example = "ENTERED") String finalStatus,
        @Schema(description = "Sesion vinculada al registro (UUID)", example = "00016ec5-90fb-4c63-aba9-3ea17abd27c0") String sessionId,
        @Schema(description = "Puerta asociada al ultimo movimiento", example = "7") Long gateId,
        @Schema(description = "Tipo de movimiento registrado") AccessType accessType,
        @Schema(description = "Fecha y hora del registro", example = "2026-05-09T10:00:00Z") OffsetDateTime entryAt
) {}
