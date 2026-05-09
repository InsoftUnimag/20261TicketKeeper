package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.ProcessEntryAttemptCommand;
import com.empresa.ingreso.application.port.in.ProcessEntryAttemptResult;
import com.empresa.ingreso.application.port.in.ProcessEntryAttemptUseCase;
import com.empresa.ingreso.application.port.out.LoadEventSessionPort;
import com.empresa.ingreso.application.port.out.LoadReaderDevicePort;
import com.empresa.ingreso.application.port.out.LoadTicketPort;
import com.empresa.ingreso.application.port.out.SaveAccessAttemptPort;
import com.empresa.ingreso.application.port.out.SaveEntryRecordPort;
import com.empresa.ingreso.application.port.out.SaveTicketPort;
import com.empresa.ingreso.domain.model.AccessAttempt;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.domain.model.EventSession;
import com.empresa.ingreso.domain.model.ReaderDevice;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.domain.model.AttemptResult;
import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.shared.errors.ErrorCode;
import com.empresa.ingreso.shared.errors.TechnicalException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultProcessEntryAttemptUseCase implements ProcessEntryAttemptUseCase {

    private static final String APPROVED_STATUS = "APPROVED";
    private static final String REJECTED_STATUS = "REJECTED";

    private final LoadReaderDevicePort loadReaderDevicePort;
    private final LoadTicketPort loadTicketPort;
    private final SaveTicketPort saveTicketPort;
    private final LoadEventSessionPort loadEventSessionPort;
    private final SaveAccessAttemptPort saveAccessAttemptPort;
    private final SaveEntryRecordPort saveEntryRecordPort;
    private final Clock clock;

    @Autowired
    public DefaultProcessEntryAttemptUseCase(
            LoadReaderDevicePort loadReaderDevicePort,
            LoadTicketPort loadTicketPort,
            SaveTicketPort saveTicketPort,
            LoadEventSessionPort loadEventSessionPort,
            SaveAccessAttemptPort saveAccessAttemptPort,
            SaveEntryRecordPort saveEntryRecordPort
    ) {
        this(
                loadReaderDevicePort,
                loadTicketPort,
                saveTicketPort,
                loadEventSessionPort,
                saveAccessAttemptPort,
                saveEntryRecordPort,
                Clock.systemUTC()
        );
    }

    DefaultProcessEntryAttemptUseCase(
            LoadReaderDevicePort loadReaderDevicePort,
            LoadTicketPort loadTicketPort,
            SaveTicketPort saveTicketPort,
            LoadEventSessionPort loadEventSessionPort,
            SaveAccessAttemptPort saveAccessAttemptPort,
            SaveEntryRecordPort saveEntryRecordPort,
            Clock clock
    ) {
        this.loadReaderDevicePort = loadReaderDevicePort;
        this.loadTicketPort = loadTicketPort;
        this.saveTicketPort = saveTicketPort;
        this.loadEventSessionPort = loadEventSessionPort;
        this.saveAccessAttemptPort = saveAccessAttemptPort;
        this.saveEntryRecordPort = saveEntryRecordPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ProcessEntryAttemptResult execute(ProcessEntryAttemptCommand command) {
        OffsetDateTime now = OffsetDateTime.now(clock);

        try {
            Optional<ReaderDevice> maybeReader = validateReader(command);
            if (maybeReader.isEmpty()) {
                AccessAttempt attempt = persistAttempt(now, command, null, AttemptResult.REJECTED, ErrorCode.LECTOR_NO_CONFIGURADO);
                return rejected("Reader device is not configured", ErrorCode.LECTOR_NO_CONFIGURADO, attempt.id());
            }
            ReaderDevice reader = maybeReader.get();

            Optional<Ticket> maybeTicket = loadTicketPort.findByCodeForUpdate(command.ticketCode());
            if (maybeTicket.isEmpty()) {
                AccessAttempt attempt = persistAttempt(now, command, null, AttemptResult.REJECTED, ErrorCode.TICKET_NO_ENCONTRADO);
                return rejected("Ticket was not found", ErrorCode.TICKET_NO_ENCONTRADO, attempt.id());
            }

            Ticket ticket = maybeTicket.get();

            if (ticket.used() || ticket.status() == TicketStatus.ENTERED) {
                AccessAttempt attempt = persistAttempt(now, command, ticket.id(), AttemptResult.REJECTED, ErrorCode.TICKET_DUPLICADO);
                return rejected("Ticket has already been used", ErrorCode.TICKET_DUPLICADO, attempt.id());
            }

            if (ticket.status() != TicketStatus.ACTIVE) {
                AccessAttempt attempt = persistAttempt(now, command, ticket.id(), AttemptResult.REJECTED, ErrorCode.ESTADO_INVALIDO);
                return rejected("Ticket status does not allow entry", ErrorCode.ESTADO_INVALIDO, attempt.id());
            }

            if (!validateSession(command, ticket)) {
                AccessAttempt attempt = persistAttempt(now, command, ticket.id(), AttemptResult.REJECTED, ErrorCode.SESION_INVALIDA);
                return rejected("Event session is invalid", ErrorCode.SESION_INVALIDA, attempt.id());
            }

            if (!validateZone(reader, ticket)) {
                AccessAttempt attempt = persistAttempt(now, command, ticket.id(), AttemptResult.REJECTED, ErrorCode.ZONA_INCORRECTA);
                return rejected("Reader zone does not match ticket zone", ErrorCode.ZONA_INCORRECTA, attempt.id());
            }

            saveEntryRecordPort.save(new EntryRecord(
                    null,
                    ticket.id(),
                    command.sessionId(),
                    command.gateId(),
                    AccessType.ENTRY,
                    now
            ));

            Ticket enteredTicket = new Ticket(
                    ticket.id(),
                    ticket.code(),
                    TicketStatus.ENTERED,
                    ticket.category(),
                    ticket.allowedZone(),
                    ticket.sessionId(),
                    true
            );
            saveTicketPort.save(enteredTicket);

            AccessAttempt approvedAttempt = persistAttempt(now, command, ticket.id(), AttemptResult.APPROVED, null);
            return approved("Entry authorized", approvedAttempt.id());
        } catch (TechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new TechnicalException("Technical error while processing the entry attempt", e);
        }
    }

    private Optional<ReaderDevice> validateReader(ProcessEntryAttemptCommand command) {
        return loadReaderDevicePort.findReaderDeviceById(command.readerId())
                .filter(ReaderDevice::enabled)
                .filter(reader -> reader.gateId().equals(command.gateId()));
    }

    private boolean validateSession(ProcessEntryAttemptCommand command, Ticket ticket) {
        if (!ticket.sessionId().equals(command.sessionId())) {
            return false;
        }
        Optional<EventSession> maybeSession = loadEventSessionPort.findEventSessionById(command.sessionId());
        return maybeSession.filter(EventSession::active).isPresent();
    }

    private boolean validateZone(ReaderDevice reader, Ticket ticket) {
        return ticket.allowedZone().equals(reader.assignedZone());
    }

    private AccessAttempt persistAttempt(
            OffsetDateTime now,
            ProcessEntryAttemptCommand command,
            Long ticketId,
            AttemptResult result,
            ErrorCode errorCode
    ) {
        return saveAccessAttemptPort.save(new AccessAttempt(
                null,
                ticketId,
                command.ticketCode(),
                command.readerId(),
                command.gateId(),
                command.sessionId(),
                command.channel(),
                result,
                errorCode,
                now
        ));
    }

    private ProcessEntryAttemptResult approved(String message, Long attemptId) {
        return new ProcessEntryAttemptResult(APPROVED_STATUS, message, null, attemptId);
    }

    private ProcessEntryAttemptResult rejected(String message, ErrorCode errorCode, Long attemptId) {
        return new ProcessEntryAttemptResult(REJECTED_STATUS, message, errorCode, attemptId);
    }
}
