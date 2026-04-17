package com.empresa.ingreso.application.dto;

import com.empresa.ingreso.shared.errors.ErrorCode;

public record ProcessEntryAttemptResponse(
        String status,
        String message,
        ErrorCode errorCode,
        Long attemptId
) {
}
