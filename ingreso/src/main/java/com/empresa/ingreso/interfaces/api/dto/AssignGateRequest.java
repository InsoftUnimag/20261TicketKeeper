package com.empresa.ingreso.interfaces.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignGateRequest(@NotNull Long sessionId, @NotNull Long gateId, @NotBlank String ticketCategory, @NotBlank String zone) {}
