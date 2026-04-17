package com.empresa.ingreso.application.dto;

import com.empresa.ingreso.domain.model.AccessChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcessEntryAttemptRequest(
        @NotBlank String ticketCode,
        @NotNull Long readerId,
        @NotNull Long gateId,
        @NotNull Long sessionId,
        @NotNull AccessChannel channel
) {
}
