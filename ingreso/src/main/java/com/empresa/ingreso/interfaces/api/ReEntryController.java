package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.application.port.in.ReEntryCommand;
import com.empresa.ingreso.application.port.in.ReEntryUseCase;
import com.empresa.ingreso.application.port.in.RegisterExitCommand;
import com.empresa.ingreso.application.port.in.RegisterExitUseCase;
import com.empresa.ingreso.interfaces.api.dto.ReEntryRequest;
import com.empresa.ingreso.interfaces.api.dto.ReEntryResponse;
import com.empresa.ingreso.interfaces.api.dto.RegisterExitRequest;
import com.empresa.ingreso.interfaces.api.dto.RegisterExitResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/access-flow")
public class ReEntryController {
    private final ReEntryUseCase reEntryUseCase;
    private final RegisterExitUseCase registerExitUseCase;

    public ReEntryController(ReEntryUseCase reEntryUseCase, RegisterExitUseCase registerExitUseCase) {
        this.reEntryUseCase = reEntryUseCase; this.registerExitUseCase = registerExitUseCase;
    }

    @PostMapping("/re-entries")
    public ResponseEntity<ReEntryResponse> reEntry(@Valid @RequestBody ReEntryRequest request) {
        var result = reEntryUseCase.execute(new ReEntryCommand(request.ticketCode(), request.readerId(), request.gateId(), request.sessionId(), request.channel()));
        return ResponseEntity.ok(new ReEntryResponse(result.status(), result.message(), result.errorCode(), result.reEntriesUsed(), result.reEntryLimit()));
    }

    @PostMapping("/exits")
    public ResponseEntity<RegisterExitResponse> exit(@Valid @RequestBody RegisterExitRequest request) {
        var result = registerExitUseCase.execute(new RegisterExitCommand(request.ticketCode(), request.readerId(), request.gateId(), request.sessionId(), request.channel()));
        return ResponseEntity.ok(new RegisterExitResponse(result.status(), result.message(), result.errorCode()));
    }
}
