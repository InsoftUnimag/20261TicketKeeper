package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.RegisterExitCommand;
import com.empresa.ingreso.application.port.in.RegisterExitResult;
import com.empresa.ingreso.application.port.in.RegisterExitUseCase;
import com.empresa.ingreso.application.port.out.LoadTicketPort;
import com.empresa.ingreso.application.port.out.SaveAccessAttemptPort;
import com.empresa.ingreso.application.port.out.SaveEntryRecordPort;
import com.empresa.ingreso.application.port.out.SaveTicketPort;
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
    private final LoadTicketPort loadTicketPort;
    private final SaveTicketPort saveTicketPort;
    private final SaveAccessAttemptPort saveAccessAttemptPort;
    private final SaveEntryRecordPort saveEntryRecordPort;
    private final Clock clock;

    @Autowired
    public DefaultRegisterExitUseCase(LoadTicketPort loadTicketPort, SaveTicketPort saveTicketPort, SaveAccessAttemptPort saveAccessAttemptPort, SaveEntryRecordPort saveEntryRecordPort) {
        this(loadTicketPort, saveTicketPort, saveAccessAttemptPort, saveEntryRecordPort, Clock.systemUTC());
    }

    DefaultRegisterExitUseCase(LoadTicketPort loadTicketPort, SaveTicketPort saveTicketPort, SaveAccessAttemptPort saveAccessAttemptPort, SaveEntryRecordPort saveEntryRecordPort, Clock clock) {
        this.loadTicketPort = loadTicketPort; this.saveTicketPort = saveTicketPort; this.saveAccessAttemptPort = saveAccessAttemptPort; this.saveEntryRecordPort = saveEntryRecordPort; this.clock = clock;
    }

    @Override
    @Transactional
    public RegisterExitResult execute(RegisterExitCommand command) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        var maybeTicket = loadTicketPort.findByCodeForUpdate(command.ticketCode());
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
