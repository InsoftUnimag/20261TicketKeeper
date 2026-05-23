package com.empresa.ingreso.application.port.in;

import java.util.Optional;

public interface ProcessEntryAttemptUseCase {

    Optional<ProcessEntryAttemptResult> execute(ProcessEntryAttemptCommand command);
}
