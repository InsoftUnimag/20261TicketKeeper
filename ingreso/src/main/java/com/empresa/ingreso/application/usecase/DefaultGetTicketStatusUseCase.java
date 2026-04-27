package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.GetTicketStatusCommand;
import com.empresa.ingreso.application.port.in.GetTicketStatusResult;
import com.empresa.ingreso.application.port.in.GetTicketStatusUseCase;
import com.empresa.ingreso.application.port.out.LoadEntryRecordPort;
import com.empresa.ingreso.application.port.out.LoadTicketByCodePort;
import com.empresa.ingreso.application.port.out.SaveTicketStatusQueryPort;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.domain.model.TicketStatusQuery;
import com.empresa.ingreso.shared.errors.ErrorCode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultGetTicketStatusUseCase implements GetTicketStatusUseCase {
    private final LoadTicketByCodePort loadTicketByCodePort;
    private final LoadEntryRecordPort loadEntryRecordPort;
    private final SaveTicketStatusQueryPort saveTicketStatusQueryPort;
    private final Clock clock;

    @Autowired
    public DefaultGetTicketStatusUseCase(LoadTicketByCodePort loadTicketByCodePort, LoadEntryRecordPort loadEntryRecordPort, SaveTicketStatusQueryPort saveTicketStatusQueryPort) {
        this(loadTicketByCodePort, loadEntryRecordPort, saveTicketStatusQueryPort, Clock.systemUTC());
    }

    DefaultGetTicketStatusUseCase(LoadTicketByCodePort loadTicketByCodePort, LoadEntryRecordPort loadEntryRecordPort, SaveTicketStatusQueryPort saveTicketStatusQueryPort, Clock clock) {
        this.loadTicketByCodePort = loadTicketByCodePort;
        this.loadEntryRecordPort = loadEntryRecordPort;
        this.saveTicketStatusQueryPort = saveTicketStatusQueryPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public GetTicketStatusResult execute(GetTicketStatusCommand command) {
        saveTicketStatusQueryPort.save(new TicketStatusQuery(null, command.ticketCode(), OffsetDateTime.now(clock), command.requestedBy()));
        Optional<Ticket> maybeTicket = loadTicketByCodePort.findByCode(command.ticketCode());
        if (maybeTicket.isEmpty()) {
            return new GetTicketStatusResult("NOT_FOUND", "Ticket no encontrado", ErrorCode.TICKET_NO_ENCONTRADO, null, command.ticketCode(), null, null, null);
        }
        Ticket ticket = maybeTicket.get();
        Optional<EntryRecord> record = loadEntryRecordPort.findLatestByTicketId(ticket.id());
        if (ticket.status() == TicketStatus.CANCELED) return response(ticket, record, "INVALID", "Ticket cancelado - ingreso no permitido", ErrorCode.ESTADO_INVALIDO);
        if (ticket.status() == TicketStatus.BLOCKED) return response(ticket, record, "INVALID", "Ticket bloqueado", ErrorCode.ESTADO_INVALIDO);
        if (ticket.used() || ticket.status() == TicketStatus.ENTERED || ticket.status() == TicketStatus.EXITED || record.isPresent()) return response(ticket, record, "USED", "Ticket ya utilizado", null);
        if (ticket.status() == TicketStatus.ACTIVE) return response(ticket, record, "VALID", "Ticket valido - no utilizado", null);
        return response(ticket, record, "INVALID", "Ticket con datos inconsistentes", ErrorCode.ESTADO_INVALIDO);
    }

    private GetTicketStatusResult response(Ticket ticket, Optional<EntryRecord> record, String status, String message, ErrorCode errorCode) {
        EntryRecord r = record.orElse(null);
        return new GetTicketStatusResult(status, message, errorCode, ticket.status(), ticket.code(), ticket.sessionId(), r == null ? null : r.gateId(), r == null ? null : r.enteredAt());
    }
}
