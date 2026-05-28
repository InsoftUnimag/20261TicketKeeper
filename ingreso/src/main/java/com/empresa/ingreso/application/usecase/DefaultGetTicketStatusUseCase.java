package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.GetTicketStatusCommand;
import com.empresa.ingreso.application.port.in.GetTicketStatusResult;
import com.empresa.ingreso.application.port.in.GetTicketStatusUseCase;
import com.empresa.ingreso.application.port.out.LoadEntryRecordPort;
import com.empresa.ingreso.application.port.out.LoadTicketByCodePort;
import com.empresa.ingreso.application.port.out.Modulo1Port;
import com.empresa.ingreso.application.port.out.SaveTicketStatusQueryPort;
import com.empresa.ingreso.application.service.Modulo1TicketImportService;
import com.empresa.ingreso.application.service.RecintoInfoService;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.domain.model.TicketStatusQuery;
import com.empresa.ingreso.interfaces.api.dto.Modulo1EventoDto;
import com.empresa.ingreso.shared.errors.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

@Component
public class DefaultGetTicketStatusUseCase implements GetTicketStatusUseCase {

    private final LoadTicketByCodePort loadTicketByCodePort;
    private final LoadEntryRecordPort loadEntryRecordPort;
    private final SaveTicketStatusQueryPort saveTicketStatusQueryPort;
    private final Modulo1TicketImportService modulo1TicketImportService;
    private final Modulo1Port modulo1Port;
    private final RecintoInfoService recintoInfoService;
    private final Clock clock;

    // Único constructor para la inyección de dependencias de Spring
    public DefaultGetTicketStatusUseCase(
            LoadTicketByCodePort loadTicketByCodePort,
            LoadEntryRecordPort loadEntryRecordPort,
            SaveTicketStatusQueryPort saveTicketStatusQueryPort,
            Modulo1TicketImportService modulo1TicketImportService,
            Modulo1Port modulo1Port,
            RecintoInfoService recintoInfoService
    ) {
        this.loadTicketByCodePort = loadTicketByCodePort;
        this.loadEntryRecordPort = loadEntryRecordPort;
        this.saveTicketStatusQueryPort = saveTicketStatusQueryPort;
        this.modulo1TicketImportService = modulo1TicketImportService;
        this.modulo1Port = modulo1Port;
        this.recintoInfoService = recintoInfoService;
        this.clock = Clock.systemUTC(); // Usamos el reloj del sistema por defecto
    }

    @Override
    @Transactional(readOnly = true)
    public GetTicketStatusResult execute(GetTicketStatusCommand command) {
        saveTicketStatusQueryPort.save(new TicketStatusQuery(null, command.ticketCode(), OffsetDateTime.now(clock), command.requestedBy()));

        Optional<Ticket> maybeTicket = loadTicketByCodePort.findByCode(command.ticketCode())
                .or(() -> modulo1TicketImportService.findTransientByCode(command.ticketCode()));

        if (maybeTicket.isEmpty()) {
            return new GetTicketStatusResult("NOT_FOUND", "Ticket no encontrado", ErrorCode.TICKET_NO_ENCONTRADO, null, command.ticketCode(), null, null, null, null, null, null);
        }

        Ticket ticket = maybeTicket.get();
        Optional<EntryRecord> record = ticket.id() == null ? Optional.empty() : loadEntryRecordPort.findLatestByTicketId(ticket.id());

        // Enriquecer con información del Módulo 1
        Optional<Modulo1EventoDto> eventoInfo = Optional.ofNullable(ticket.sessionId()).flatMap(modulo1Port::findActiveEventById);
        String eventName = eventoInfo.map(Modulo1EventoDto::nombre).orElse(null);
        String recintoName = eventoInfo.flatMap(e -> recintoInfoService.getRecintoNameById(e.recintoId())).orElse(null);

        if (ticket.status() == TicketStatus.CANCELED) return response(ticket, record, "INVALID", "Ticket cancelado - ingreso no permitido", ErrorCode.ESTADO_INVALIDO, eventName, recintoName);
        if (ticket.status() == TicketStatus.BLOCKED) return response(ticket, record, "INVALID", "Ticket bloqueado", ErrorCode.ESTADO_INVALIDO, eventName, recintoName);
        if (ticket.used() || ticket.status() == TicketStatus.ENTERED || ticket.status() == TicketStatus.EXITED || record.isPresent()) return response(ticket, record, "USED", "Ticket ya utilizado", null, eventName, recintoName);
        if (ticket.status() == TicketStatus.ACTIVE) return response(ticket, record, "VALID", "Ticket valido - no utilizado", null, eventName, recintoName);

        return response(ticket, record, "INVALID", "Ticket con datos inconsistentes", ErrorCode.ESTADO_INVALIDO, eventName, recintoName);
    }

    private GetTicketStatusResult response(Ticket ticket, Optional<EntryRecord> record, String status, String message, ErrorCode errorCode, String eventName, String recintoName) {
        EntryRecord r = record.orElse(null);
        return new GetTicketStatusResult(
                status,
                message,
                errorCode,
                ticket.status(),
                ticket.code(),
                ticket.sessionId(),
                eventName,
                recintoName,
                ticket.allowedZone(),
                r == null ? null : r.gateId(),
                r == null ? null : r.enteredAt()
        );
    }
}
