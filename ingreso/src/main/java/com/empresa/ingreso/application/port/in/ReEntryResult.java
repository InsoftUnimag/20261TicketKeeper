package com.empresa.ingreso.application.port.in;

import com.empresa.ingreso.shared.errors.ErrorCode;

public record ReEntryResult(String status, String message, ErrorCode errorCode, Integer reEntriesUsed, Integer reEntryLimit) {}
