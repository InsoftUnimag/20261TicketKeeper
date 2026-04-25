package com.empresa.ingreso.shared.errors;

public class TechnicalException extends RuntimeException {

    private final ErrorCode errorCode;

    public TechnicalException(String message) {
        super(message);
        this.errorCode = ErrorCode.ERROR_TECNICO;
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.ERROR_TECNICO;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
