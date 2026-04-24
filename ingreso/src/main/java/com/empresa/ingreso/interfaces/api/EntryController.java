package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.ProcessEntryAttemptCommand;
import com.empresa.ingreso.application.port.in.ProcessEntryAttemptResult;
import com.empresa.ingreso.application.port.in.ProcessEntryAttemptUseCase;
import com.empresa.ingreso.interfaces.api.dto.ProcessEntryAttemptRequest;
import com.empresa.ingreso.interfaces.api.dto.ProcessEntryAttemptResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entry-attempts")
public class EntryController {

    private final ProcessEntryAttemptUseCase processEntryAttemptUseCase;

    public EntryController(ProcessEntryAttemptUseCase processEntryAttemptUseCase) {
        this.processEntryAttemptUseCase = processEntryAttemptUseCase;
    }

    @PostMapping
    public ResponseEntity<ProcessEntryAttemptResponse> process(@Valid @RequestBody ProcessEntryAttemptRequest request) {
        ProcessEntryAttemptResult result = processEntryAttemptUseCase.execute(new ProcessEntryAttemptCommand(
                request.ticketCode(),
                request.readerId(),
                request.gateId(),
                request.sessionId(),
                request.channel()
        ));
        ProcessEntryAttemptResponse response = new ProcessEntryAttemptResponse(
                result.status(),
                result.message(),
                result.errorCode(),
                result.attemptId()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
