package com.empresa.ingreso.interfaces.api.dto;

import com.empresa.ingreso.shared.errors.ErrorCode;

public record ReEntryResponse(String status, String message, ErrorCode errorCode, Integer reEntriesUsed, Integer reEntryLimit) {}
