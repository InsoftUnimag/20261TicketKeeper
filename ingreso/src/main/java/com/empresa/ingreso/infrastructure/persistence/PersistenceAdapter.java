package com.empresa.ingreso.infrastructure.persistence;

import com.empresa.ingreso.application.port.out.LoadEntryRecordPort;
import com.empresa.ingreso.application.port.out.LoadEventSessionPort;
import com.empresa.ingreso.application.port.out.LoadGateAssignmentPort;
import com.empresa.ingreso.application.port.out.LoadReaderDevicePort;
import com.empresa.ingreso.application.port.out.LoadTicketByCodePort;
import com.empresa.ingreso.application.port.out.LoadTicketPort;
import com.empresa.ingreso.application.port.out.PublishGateAssignmentPort;
import com.empresa.ingreso.application.port.out.SaveAccessAttemptPort;
import com.empresa.ingreso.application.port.out.SaveEntryRecordPort;
import com.empresa.ingreso.application.port.out.SaveGateAssignmentPort;
import com.empresa.ingreso.application.port.out.SaveTicketPort;
import com.empresa.ingreso.application.port.out.SaveTicketStatusQueryPort;
import com.empresa.ingreso.domain.model.AccessAttempt;
import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.domain.model.EventSession;
import com.empresa.ingreso.domain.model.GateAssignment;
import com.empresa.ingreso.domain.model.ReaderDevice;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.domain.model.TicketStatusQuery;
import com.empresa.ingreso.infrastructure.persistence.entity.AccessAttemptEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EventSessionEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.GateAssignmentEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketStatusQueryEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataAccessAttemptRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEventSessionRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataGateAssignmentRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataReaderDeviceRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketStatusQueryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PersistenceAdapter implements
        LoadReaderDevicePort,
        LoadTicketPort,
        LoadTicketByCodePort,
        LoadEventSessionPort,
        LoadEntryRecordPort,
        LoadGateAssignmentPort,
        SaveAccessAttemptPort,
        SaveEntryRecordPort,
        SaveTicketPort,
        SaveGateAssignmentPort,
        SaveTicketStatusQueryPort,
        PublishGateAssignmentPort {

    private final SpringDataReaderDeviceRepository readerDeviceRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataEventSessionRepository eventSessionRepository;
    private final SpringDataAccessAttemptRepository accessAttemptRepository;
    private final SpringDataEntryRecordRepository entryRecordRepository;
    private final SpringDataGateAssignmentRepository gateAssignmentRepository;
    private final SpringDataTicketStatusQueryRepository ticketStatusQueryRepository;

    public PersistenceAdapter(
            SpringDataReaderDeviceRepository readerDeviceRepository,
            SpringDataTicketRepository ticketRepository,
            SpringDataEventSessionRepository eventSessionRepository,
            SpringDataAccessAttemptRepository accessAttemptRepository,
            SpringDataEntryRecordRepository entryRecordRepository,
            SpringDataGateAssignmentRepository gateAssignmentRepository,
            SpringDataTicketStatusQueryRepository ticketStatusQueryRepository
    ) {
        this.readerDeviceRepository = readerDeviceRepository;
        this.ticketRepository = ticketRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.accessAttemptRepository = accessAttemptRepository;
        this.entryRecordRepository = entryRecordRepository;
        this.gateAssignmentRepository = gateAssignmentRepository;
        this.ticketStatusQueryRepository = ticketStatusQueryRepository;
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
    public Optional<Ticket> findByCode(String code) {
        return ticketRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public Optional<EventSession> findEventSessionById(Long sessionId) {
        return eventSessionRepository.findById(sessionId).map(this::toDomain);
    }

    @Override
    public Optional<EntryRecord> findLatestByTicketId(Long ticketId) {
        return entryRecordRepository.findFirstByTicketIdOrderByEnteredAtDesc(ticketId).map(this::toDomain);
    }

    @Override
    public List<EntryRecord> findByEventId(Long eventId) {
        return entryRecordRepository.findByEventIdOrderByEnteredAtAsc(eventId).stream().map(this::toDomain).toList();
    }

    @Override
    public long countReEntriesByTicketId(Long ticketId) {
        return entryRecordRepository.countByTicketIdAndAccessType(ticketId, AccessType.RE_ENTRY);
    }

    @Override
    public Optional<GateAssignment> findActiveBySessionGateAndCategory(Long sessionId, Long gateId, String ticketCategory) {
        return gateAssignmentRepository
                .findFirstBySessionIdAndGateIdAndTicketCategoryAndActiveTrue(sessionId, gateId, ticketCategory)
                .map(this::toDomain);
    }

    @Override
    public boolean existsActiveConflict(Long sessionId, Long gateId, String ticketCategory) {
        return gateAssignmentRepository.existsBySessionIdAndGateIdAndTicketCategoryAndActiveTrue(
                sessionId,
                gateId,
                ticketCategory
        );
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
        return toDomain(entryRecordRepository.save(entity));
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
    public GateAssignment save(GateAssignment assignment) {
        GateAssignmentEntity entity = new GateAssignmentEntity();
        entity.setId(assignment.id());
        entity.setSessionId(assignment.sessionId());
        entity.setGateId(assignment.gateId());
        entity.setTicketCategory(assignment.ticketCategory());
        entity.setZone(assignment.zone());
        entity.setActive(assignment.active());
        return toDomain(gateAssignmentRepository.save(entity));
    }

    @Override
    public TicketStatusQuery save(TicketStatusQuery query) {
        TicketStatusQueryEntity entity = new TicketStatusQueryEntity();
        entity.setId(query.id());
        entity.setTicketCode(query.ticketCode());
        entity.setQueriedAt(query.queriedAt());
        entity.setRequestedBy(query.requestedBy());
        return toDomain(ticketStatusQueryRepository.save(entity));
    }

    @Override
    public void publish(GateAssignment assignment) {
        // No-op: publication is outside the current persistence scope.
    }

    private ReaderDevice toDomain(ReaderDeviceEntity entity) {
        return new ReaderDevice(entity.getId(), entity.getGateId(), entity.getAssignedZone(), entity.isEnabled());
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

    private EntryRecord toDomain(EntryRecordEntity entity) {
        return new EntryRecord(
                entity.getTicketId(),
                entity.getEventId(),
                entity.getGateId(),
                entity.getAccessType(),
                entity.getEnteredAt()
        );
    }

    private GateAssignment toDomain(GateAssignmentEntity entity) {
        return new GateAssignment(
                entity.getId(),
                entity.getSessionId(),
                entity.getGateId(),
                entity.getTicketCategory(),
                entity.getZone(),
                entity.isActive()
        );
    }

    private TicketStatusQuery toDomain(TicketStatusQueryEntity entity) {
        return new TicketStatusQuery(
                entity.getId(),
                entity.getTicketCode(),
                entity.getQueriedAt(),
                entity.getRequestedBy()
        );
    }
}
