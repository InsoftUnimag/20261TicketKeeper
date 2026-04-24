package com.empresa.ingreso.application.port.in;

public interface ProcessEntryAttemptUseCase {

    ProcessEntryAttemptResult execute(ProcessEntryAttemptCommand command);
}
