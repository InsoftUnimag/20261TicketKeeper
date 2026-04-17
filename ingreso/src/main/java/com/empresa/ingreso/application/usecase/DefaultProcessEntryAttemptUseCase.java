package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.dto.ProcessEntryAttemptRequest;
import com.empresa.ingreso.application.dto.ProcessEntryAttemptResponse;
import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.domain.model.AttemptResult;
import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.infrastructure.persistence.entity.AccessAttemptEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EventSessionEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataAccessAttemptRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEventSessionRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataReaderDeviceRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketRepository;
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

    private final SpringDataReaderDeviceRepository readerDeviceRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataEventSessionRepository eventSessionRepository;
    private final SpringDataAccessAttemptRepository accessAttemptRepository;
    private final SpringDataEntryRecordRepository entryRecordRepository;
    private final Clock clock;

    @Autowired
    public DefaultProcessEntryAttemptUseCase(
            SpringDataReaderDeviceRepository readerDeviceRepository,
            SpringDataTicketRepository ticketRepository,
            SpringDataEventSessionRepository eventSessionRepository,
            SpringDataAccessAttemptRepository accessAttemptRepository,
            SpringDataEntryRecordRepository entryRecordRepository
    ) {
        this(
                readerDeviceRepository,
                ticketRepository,
                eventSessionRepository,
                accessAttemptRepository,
                entryRecordRepository,
                Clock.systemUTC()
        );
    }

    DefaultProcessEntryAttemptUseCase(
            SpringDataReaderDeviceRepository readerDeviceRepository,
            SpringDataTicketRepository ticketRepository,
            SpringDataEventSessionRepository eventSessionRepository,
            SpringDataAccessAttemptRepository accessAttemptRepository,
            SpringDataEntryRecordRepository entryRecordRepository,
            Clock clock
    ) {
        this.readerDeviceRepository = readerDeviceRepository;
        this.ticketRepository = ticketRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.accessAttemptRepository = accessAttemptRepository;
        this.entryRecordRepository = entryRecordRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ProcessEntryAttemptResponse execute(ProcessEntryAttemptRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);

        try {
            // 1) Validar configuracion del lector y puerta.
            Optional<ReaderDeviceEntity> maybeReader = validateReader(request);
            if (maybeReader.isEmpty()) {
                AccessAttemptEntity attempt = persistAttempt(now, request, null, AttemptResult.REJECTED, ErrorCode.LECTOR_NO_CONFIGURADO);
                return new ProcessEntryAttemptResponse("RECHAZADO", "Lector no configurado", ErrorCode.LECTOR_NO_CONFIGURADO, attempt.getId());
            }
            ReaderDeviceEntity reader = maybeReader.get();

            // 2) Validar existencia del ticket (lock para evitar doble ingreso por concurrencia).
            Optional<TicketEntity> maybeTicket = ticketRepository.findByCodeForUpdate(request.ticketCode());
            if (maybeTicket.isEmpty()) {
                AccessAttemptEntity attempt = persistAttempt(now, request, null, AttemptResult.REJECTED, ErrorCode.TICKET_NO_ENCONTRADO);
                return new ProcessEntryAttemptResponse("RECHAZADO", "Ticket no encontrado", ErrorCode.TICKET_NO_ENCONTRADO, attempt.getId());
            }

            TicketEntity ticket = maybeTicket.get();

            // 3) Validar estado del ticket.
            if (ticket.getStatus() != TicketStatus.ACTIVE) {
                AccessAttemptEntity attempt = persistAttempt(now, request, ticket.getId(), AttemptResult.REJECTED, ErrorCode.ESTADO_INVALIDO);
                return new ProcessEntryAttemptResponse("RECHAZADO", "Estado de ticket invalido para ingreso", ErrorCode.ESTADO_INVALIDO, attempt.getId());
            }

            // 4) Validar sesion activa del evento.
            if (!validateSession(request, ticket)) {
                AccessAttemptEntity attempt = persistAttempt(now, request, ticket.getId(), AttemptResult.REJECTED, ErrorCode.SESION_INVALIDA);
                return new ProcessEntryAttemptResponse("RECHAZADO", "Sesion invalida", ErrorCode.SESION_INVALIDA, attempt.getId());
            }

            // 5) Validar zona o puerta autorizada.
            if (!validateZone(reader, ticket)) {
                AccessAttemptEntity attempt = persistAttempt(now, request, ticket.getId(), AttemptResult.REJECTED, ErrorCode.ZONA_INCORRECTA);
                return new ProcessEntryAttemptResponse("RECHAZADO", "Zona incorrecta", ErrorCode.ZONA_INCORRECTA, attempt.getId());
            }

            // 6) Validar duplicidad.
            if (ticket.isUsed() || ticket.getStatus() == TicketStatus.ENTERED) {
                AccessAttemptEntity attempt = persistAttempt(now, request, ticket.getId(), AttemptResult.REJECTED, ErrorCode.TICKET_DUPLICADO);
                return new ProcessEntryAttemptResponse("RECHAZADO", "Ticket duplicado", ErrorCode.TICKET_DUPLICADO, attempt.getId());
            }

            // Crear registro de ingreso y actualizar ticket.
            EntryRecordEntity record = new EntryRecordEntity();
            record.setTicketId(ticket.getId());
            record.setEventId(request.sessionId()); // En este bounded-context usamos sessionId como correlativo del evento/sesion.
            record.setGateId(request.gateId());
            record.setAccessType(AccessType.ENTRY);
            record.setEnteredAt(now);
            entryRecordRepository.save(record);

            ticket.setStatus(TicketStatus.ENTERED);
            ticket.setUsed(true);
            ticketRepository.save(ticket);

            AccessAttemptEntity approvedAttempt = persistAttempt(now, request, ticket.getId(), AttemptResult.APPROVED, null);
            return new ProcessEntryAttemptResponse("APROBADO", "Ingreso autorizado", null, approvedAttempt.getId());
        } catch (TechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new TechnicalException("Error tecnico al procesar intento de ingreso");
        }
    }

    private Optional<ReaderDeviceEntity> validateReader(ProcessEntryAttemptRequest request) {
        Optional<ReaderDeviceEntity> maybeReader = readerDeviceRepository.findById(request.readerId());
        if (maybeReader.isEmpty()) {
            return Optional.empty();
        }

        ReaderDeviceEntity reader = maybeReader.get();
        if (!reader.isEnabled()
                || reader.getAssignedZone() == null
                || reader.getAssignedZone().isBlank()
                || reader.getGateId() == null
                || !reader.getGateId().equals(request.gateId())) {
            return Optional.empty();
        }
        return Optional.of(reader);
    }

    private boolean validateSession(ProcessEntryAttemptRequest request, TicketEntity ticket) {
        if (!ticket.getSessionId().equals(request.sessionId())) {
            return false;
        }
        Optional<EventSessionEntity> maybeSession = eventSessionRepository.findById(request.sessionId());
        return maybeSession.filter(EventSessionEntity::isActive).isPresent();
    }

    private boolean validateZone(ReaderDeviceEntity reader, TicketEntity ticket) {
        return ticket.getAllowedZone() != null
                && reader.getAssignedZone() != null
                && ticket.getAllowedZone().equals(reader.getAssignedZone());
    }

    private AccessAttemptEntity persistAttempt(
            OffsetDateTime now,
            ProcessEntryAttemptRequest request,
            Long ticketId,
            AttemptResult result,
            ErrorCode errorCode
    ) {
        AccessAttemptEntity attempt = new AccessAttemptEntity();
        attempt.setTicketId(ticketId);
        attempt.setEnteredTicketCode(request.ticketCode());
        attempt.setReaderId(request.readerId());
        attempt.setGateId(request.gateId());
        attempt.setSessionId(request.sessionId());
        attempt.setChannel(request.channel());
        attempt.setResult(result);
        attempt.setErrorCode(errorCode);
        attempt.setAttemptedAt(now);
        return accessAttemptRepository.save(attempt);
    }
}
