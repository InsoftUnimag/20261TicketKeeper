package com.empresa.ingreso.application.port.in;

import com.empresa.ingreso.shared.errors.ErrorCode;

public record ProcessEntryAttemptResult(
        String status,
        String message,
        ErrorCode errorCode,
        Long attemptId
) {
}
