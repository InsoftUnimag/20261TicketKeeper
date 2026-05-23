package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.RegisterExitCommand;
import com.empresa.ingreso.application.port.in.RegisterExitResult;
import com.empresa.ingreso.application.port.in.RegisterExitUseCase;
import com.empresa.ingreso.application.port.out.SaveAccessAttemptPort;
import com.empresa.ingreso.application.port.out.SaveEntryRecordPort;
import com.empresa.ingreso.application.port.out.SaveTicketPort;
import com.empresa.ingreso.application.service.Modulo1TicketImportService;
import com.empresa.ingreso.domain.model.AccessAttempt;
import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.domain.model.AttemptResult;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.shared.errors.ErrorCode;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultRegisterExitUseCase implements RegisterExitUseCase {
    private final Modulo1TicketImportService modulo1TicketImportService;
    private final SaveTicketPort saveTicketPort;
    private final SaveAccessAttemptPort saveAccessAttemptPort;
    private final SaveEntryRecordPort saveEntryRecordPort;
    private final Clock clock;

    @Autowired
    public DefaultRegisterExitUseCase(Modulo1TicketImportService modulo1TicketImportService, SaveTicketPort saveTicketPort, SaveAccessAttemptPort saveAccessAttemptPort, SaveEntryRecordPort saveEntryRecordPort) {
        this(modulo1TicketImportService, saveTicketPort, saveAccessAttemptPort, saveEntryRecordPort, Clock.systemUTC());
    }

    DefaultRegisterExitUseCase(Modulo1TicketImportService modulo1TicketImportService, SaveTicketPort saveTicketPort, SaveAccessAttemptPort saveAccessAttemptPort, SaveEntryRecordPort saveEntryRecordPort, Clock clock) {
        this.modulo1TicketImportService = modulo1TicketImportService; this.saveTicketPort = saveTicketPort; this.saveAccessAttemptPort = saveAccessAttemptPort; this.saveEntryRecordPort = saveEntryRecordPort; this.clock = clock;
    }

    @Override
    @Transactional
    public RegisterExitResult execute(RegisterExitCommand command) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        var maybeTicket = modulo1TicketImportService.resolveForSession(command.ticketCode(), command.sessionId());
        if (maybeTicket.isEmpty()) {
            persist(now, command, null, AttemptResult.REJECTED, ErrorCode.TICKET_NO_ENCONTRADO);
            return new RegisterExitResult("REJECTED", "Ticket no encontrado", ErrorCode.TICKET_NO_ENCONTRADO);
        }
        Ticket ticket = maybeTicket.get();
        if (!ticket.canRegisterExit()) {
            persist(now, command, ticket.id(), AttemptResult.REJECTED, ErrorCode.SALIDA_NO_PERMITIDA);
            return new RegisterExitResult("REJECTED", "Salida no permitida", ErrorCode.SALIDA_NO_PERMITIDA);
        }
        saveEntryRecordPort.save(new EntryRecord(null, ticket.id(), command.sessionId(), command.gateId(), AccessType.EXIT, now));
        saveTicketPort.save(ticket.markExited());
        persist(now, command, ticket.id(), AttemptResult.APPROVED, null);
        return new RegisterExitResult("APPROVED", "Salida registrada", null);
    }

    private void persist(OffsetDateTime now, RegisterExitCommand command, Long ticketId, AttemptResult result, ErrorCode errorCode) {
        saveAccessAttemptPort.save(new AccessAttempt(null, ticketId, command.ticketCode(), command.readerId(), command.gateId(), command.sessionId(), command.channel(), result, errorCode, now));
    }
}
