package com.empresa.ingreso.application.port.in;

import com.empresa.ingreso.shared.errors.ErrorCode;

public record RegisterExitResult(String status, String message, ErrorCode errorCode) {}
