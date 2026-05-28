package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.ReEntryCommand;
import com.empresa.ingreso.application.port.in.ReEntryResult;
import com.empresa.ingreso.application.port.in.ReEntryUseCase;
import com.empresa.ingreso.application.port.out.LoadEntryRecordPort;
import com.empresa.ingreso.application.port.out.Modulo1Port;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultReEntryUseCase implements ReEntryUseCase {
    private static final int DEFAULT_REENTRY_LIMIT = 2;
    private final Modulo1TicketImportService modulo1TicketImportService;
    private final SaveTicketPort saveTicketPort;
    private final Modulo1Port modulo1Port;
    private final LoadEntryRecordPort loadEntryRecordPort;
    private final SaveAccessAttemptPort saveAccessAttemptPort;
    private final SaveEntryRecordPort saveEntryRecordPort;
    private final Clock clock;

    public DefaultReEntryUseCase(Modulo1TicketImportService modulo1TicketImportService, SaveTicketPort saveTicketPort, Modulo1Port modulo1Port, LoadEntryRecordPort loadEntryRecordPort, SaveAccessAttemptPort saveAccessAttemptPort, SaveEntryRecordPort saveEntryRecordPort) {
        this.modulo1TicketImportService=modulo1TicketImportService;
        this.saveTicketPort=saveTicketPort;
        this.modulo1Port=modulo1Port;
        this.loadEntryRecordPort=loadEntryRecordPort;
        this.saveAccessAttemptPort=saveAccessAttemptPort;
        this.saveEntryRecordPort=saveEntryRecordPort;
        this.clock=Clock.systemUTC();
    }

    @Override
    @Transactional
    public ReEntryResult execute(ReEntryCommand command) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        var maybeTicket = modulo1TicketImportService.resolveForSession(command.ticketCode(), command.sessionId());
        if (maybeTicket.isEmpty()) {
            persist(now, command, null, AttemptResult.REJECTED, ErrorCode.TICKET_NO_ENCONTRADO);
            return new ReEntryResult("REJECTED", "Ticket no encontrado", ErrorCode.TICKET_NO_ENCONTRADO, 0, DEFAULT_REENTRY_LIMIT);
        }
        Ticket ticket = maybeTicket.get();
        int used = Math.toIntExact(loadEntryRecordPort.countReEntriesByTicketId(ticket.id()));
        if (ticket.isInvalidForAccess()) return reject(now, command, ticket.id(), ErrorCode.ESTADO_INVALIDO, "Ticket con estado invalido", used);
        if (!ticket.canRegisterReEntry()) return reject(now, command, ticket.id(), ErrorCode.REINGRESO_NO_PERMITIDO, "Reingreso no permitido", used);
        
        // Validar que el evento existe y está activo en el Módulo 1
        modulo1Port.findActiveEventById(command.sessionId()).orElseThrow(() -> new com.empresa.ingreso.shared.errors.BusinessException(ErrorCode.EVENTO_NO_ENCONTRADO, "Evento no encontrado o inactivo en Módulo 1"));

        if (used >= DEFAULT_REENTRY_LIMIT) return reject(now, command, ticket.id(), ErrorCode.LIMITE_REINGRESO_EXCEDIDO, "Limite de reingresos excedido", used);
        saveEntryRecordPort.save(new EntryRecord(null, ticket.id(), command.sessionId(), command.gateId(), AccessType.RE_ENTRY, now));
        saveTicketPort.save(ticket.markEntered());
        persist(now, command, ticket.id(), AttemptResult.APPROVED, null);
        return new ReEntryResult("APPROVED", "Reingreso autorizado", null, used + 1, DEFAULT_REENTRY_LIMIT);
    }

    private ReEntryResult reject(OffsetDateTime now, ReEntryCommand command, Long ticketId, ErrorCode errorCode, String message, int used) {
        persist(now, command, ticketId, AttemptResult.REJECTED, errorCode);
        return new ReEntryResult("REJECTED", message, errorCode, used, DEFAULT_REENTRY_LIMIT);
    }

    private void persist(OffsetDateTime now, ReEntryCommand command, Long ticketId, AttemptResult result, ErrorCode errorCode) {
        saveAccessAttemptPort.save(new AccessAttempt(null, ticketId, command.ticketCode(), command.readerId(), command.gateId(), command.sessionId(), command.channel(), result, errorCode, now));
    }
}
