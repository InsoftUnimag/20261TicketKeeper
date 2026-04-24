package com.empresa.ingreso.infrastructure.persistence;

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
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PersistenceAdapter implements
        LoadReaderDevicePort,
        LoadTicketPort,
        SaveTicketPort,
        LoadEventSessionPort,
        SaveAccessAttemptPort,
        SaveEntryRecordPort {

    private final SpringDataReaderDeviceRepository readerDeviceRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataEventSessionRepository eventSessionRepository;
    private final SpringDataAccessAttemptRepository accessAttemptRepository;
    private final SpringDataEntryRecordRepository entryRecordRepository;

    public PersistenceAdapter(
            SpringDataReaderDeviceRepository readerDeviceRepository,
            SpringDataTicketRepository ticketRepository,
            SpringDataEventSessionRepository eventSessionRepository,
            SpringDataAccessAttemptRepository accessAttemptRepository,
            SpringDataEntryRecordRepository entryRecordRepository
    ) {
        this.readerDeviceRepository = readerDeviceRepository;
        this.ticketRepository = ticketRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.accessAttemptRepository = accessAttemptRepository;
        this.entryRecordRepository = entryRecordRepository;
    }

    @Override
    public Optional<ReaderDevice> findReaderDeviceById(Long readerId) {
        return readerDeviceRepository.findById(readerId).map(this::toDomain);
    }

    @Override
    public Optional<Ticket> findByCodeForUpdate(String code) {
        return ticketRepository.findByCodeForUpdate(code).map(this::toDomain);
    }

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = new TicketEntity();
        entity.setId(ticket.id());
        entity.setCode(ticket.code());
        entity.setStatus(ticket.status());
        entity.setCategory(ticket.category());
        entity.setAllowedZone(ticket.allowedZone());
        entity.setSessionId(ticket.sessionId());
        entity.setUsed(ticket.used());
        return toDomain(ticketRepository.save(entity));
    }

    @Override
    public Optional<EventSession> findEventSessionById(Long sessionId) {
        return eventSessionRepository.findById(sessionId).map(this::toDomain);
    }

    @Override
    public AccessAttempt save(AccessAttempt accessAttempt) {
        AccessAttemptEntity entity = new AccessAttemptEntity();
        entity.setId(accessAttempt.id());
        entity.setTicketId(accessAttempt.ticketId());
        entity.setEnteredTicketCode(accessAttempt.enteredTicketCode());
        entity.setReaderId(accessAttempt.readerId());
        entity.setGateId(accessAttempt.gateId());
        entity.setSessionId(accessAttempt.sessionId());
        entity.setChannel(accessAttempt.channel());
        entity.setResult(accessAttempt.result());
        entity.setErrorCode(accessAttempt.errorCode());
        entity.setAttemptedAt(accessAttempt.attemptedAt());
        return toDomain(accessAttemptRepository.save(entity));
    }

    @Override
    public EntryRecord save(EntryRecord entryRecord) {
        EntryRecordEntity entity = new EntryRecordEntity();
        entity.setTicketId(entryRecord.ticketId());
        entity.setEventId(entryRecord.eventId());
        entity.setGateId(entryRecord.gateId());
        entity.setAccessType(entryRecord.accessType());
        entity.setEnteredAt(entryRecord.enteredAt());
        EntryRecordEntity saved = entryRecordRepository.save(entity);
        return new EntryRecord(
                saved.getTicketId(),
                saved.getEventId(),
                saved.getGateId(),
                saved.getAccessType(),
                saved.getEnteredAt()
        );
    }

    private ReaderDevice toDomain(ReaderDeviceEntity entity) {
        return new ReaderDevice(
                entity.getId(),
                entity.getGateId(),
                entity.getAssignedZone(),
                entity.isEnabled()
        );
    }

    private Ticket toDomain(TicketEntity entity) {
        return new Ticket(
                entity.getId(),
                entity.getCode(),
                entity.getStatus(),
                entity.getCategory(),
                entity.getAllowedZone(),
                entity.getSessionId(),
                entity.isUsed()
        );
    }

    private EventSession toDomain(EventSessionEntity entity) {
        return new EventSession(
                entity.getId(),
                entity.getEventDate(),
                entity.isActive(),
                entity.getMaxCapacity(),
                entity.getCurrentOccupancy()
        );
    }

    private AccessAttempt toDomain(AccessAttemptEntity entity) {
        return new AccessAttempt(
                entity.getId(),
                entity.getTicketId(),
                entity.getEnteredTicketCode(),
                entity.getReaderId(),
                entity.getGateId(),
                entity.getSessionId(),
                entity.getChannel(),
                entity.getResult(),
                entity.getErrorCode(),
                entity.getAttemptedAt()
        );
    }
}
