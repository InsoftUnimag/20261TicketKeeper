package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.dto.ProcessEntryAttemptRequest;
import com.empresa.ingreso.application.dto.ProcessEntryAttemptResponse;

public interface ProcessEntryAttemptUseCase {

    ProcessEntryAttemptResponse execute(ProcessEntryAttemptRequest request);
}
