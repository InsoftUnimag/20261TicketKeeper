package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.dto.ProcessEntryAttemptRequest;
import com.empresa.ingreso.application.dto.ProcessEntryAttemptResponse;
import com.empresa.ingreso.application.usecase.ProcessEntryAttemptUseCase;
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
        ProcessEntryAttemptResponse response = processEntryAttemptUseCase.execute(request);
        HttpStatus httpStatus = "APROBADO".equals(response.status()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(response);
    }
}
