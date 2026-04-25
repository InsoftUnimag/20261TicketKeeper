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
        LoadReaderDevicePort, LoadTicketPort, LoadTicketByCodePort, SaveTicketPort, LoadEventSessionPort,
        SaveAccessAttemptPort, SaveEntryRecordPort, LoadEntryRecordPort, SaveTicketStatusQueryPort,
        LoadGateAssignmentPort, SaveGateAssignmentPort, PublishGateAssignmentPort {

    private final SpringDataReaderDeviceRepository readerDeviceRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataEventSessionRepository eventSessionRepository;
    private final SpringDataAccessAttemptRepository accessAttemptRepository;
    private final SpringDataEntryRecordRepository entryRecordRepository;
    private final SpringDataTicketStatusQueryRepository ticketStatusQueryRepository;
    private final SpringDataGateAssignmentRepository gateAssignmentRepository;

    public PersistenceAdapter(
            SpringDataReaderDeviceRepository readerDeviceRepository,
            SpringDataTicketRepository ticketRepository,
            SpringDataEventSessionRepository eventSessionRepository,
            SpringDataAccessAttemptRepository accessAttemptRepository,
            SpringDataEntryRecordRepository entryRecordRepository,
            SpringDataTicketStatusQueryRepository ticketStatusQueryRepository,
            SpringDataGateAssignmentRepository gateAssignmentRepository
    ) {
        this.readerDeviceRepository = readerDeviceRepository;
        this.ticketRepository = ticketRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.accessAttemptRepository = accessAttemptRepository;
        this.entryRecordRepository = entryRecordRepository;
        this.ticketStatusQueryRepository = ticketStatusQueryRepository;
        this.gateAssignmentRepository = gateAssignmentRepository;
    }

    @Override public Optional<ReaderDevice> findReaderDeviceById(Long readerId) { return readerDeviceRepository.findById(readerId).map(this::toDomain); }
    @Override public Optional<Ticket> findByCodeForUpdate(String code) { return ticketRepository.findByCodeForUpdate(code).map(this::toDomain); }
    @Override public Optional<Ticket> findByCode(String code) { return ticketRepository.findByCode(code).map(this::toDomain); }
    @Override public Optional<EventSession> findEventSessionById(Long sessionId) { return eventSessionRepository.findById(sessionId).map(this::toDomain); }

    @Override public Ticket save(Ticket ticket) {
        TicketEntity entity = new TicketEntity();
        entity.setId(ticket.id()); entity.setCode(ticket.code()); entity.setStatus(ticket.status()); entity.setCategory(ticket.category()); entity.setAllowedZone(ticket.allowedZone()); entity.setSessionId(ticket.sessionId()); entity.setUsed(ticket.used());
        return toDomain(ticketRepository.save(entity));
    }

    @Override public AccessAttempt save(AccessAttempt accessAttempt) {
        AccessAttemptEntity entity = new AccessAttemptEntity();
        entity.setId(accessAttempt.id()); entity.setTicketId(accessAttempt.ticketId()); entity.setEnteredTicketCode(accessAttempt.enteredTicketCode()); entity.setReaderId(accessAttempt.readerId()); entity.setGateId(accessAttempt.gateId()); entity.setSessionId(accessAttempt.sessionId()); entity.setChannel(accessAttempt.channel()); entity.setResult(accessAttempt.result()); entity.setErrorCode(accessAttempt.errorCode()); entity.setAttemptedAt(accessAttempt.attemptedAt());
        return toDomain(accessAttemptRepository.save(entity));
    }

    @Override public EntryRecord save(EntryRecord entryRecord) {
        EntryRecordEntity entity = new EntryRecordEntity();
        entity.setTicketId(entryRecord.ticketId()); entity.setEventId(entryRecord.eventId()); entity.setGateId(entryRecord.gateId()); entity.setAccessType(entryRecord.accessType()); entity.setEnteredAt(entryRecord.enteredAt());
        return toDomain(entryRecordRepository.save(entity));
    }

    @Override public Optional<EntryRecord> findLatestByTicketId(Long ticketId) { return entryRecordRepository.findFirstByTicketIdOrderByEnteredAtDesc(ticketId).map(this::toDomain); }
    @Override public List<EntryRecord> findByEventId(Long eventId) { return entryRecordRepository.findByEventIdOrderByEnteredAtAsc(eventId).stream().map(this::toDomain).toList(); }
    @Override public long countReEntriesByTicketId(Long ticketId) { return entryRecordRepository.countByTicketIdAndAccessType(ticketId, AccessType.RE_ENTRY); }

    @Override public TicketStatusQuery save(TicketStatusQuery query) {
        TicketStatusQueryEntity entity = new TicketStatusQueryEntity();
        entity.setId(query.id()); entity.setTicketCode(query.ticketCode()); entity.setQueriedAt(query.queriedAt()); entity.setRequestedBy(query.requestedBy());
        TicketStatusQueryEntity saved = ticketStatusQueryRepository.save(entity);
        return new TicketStatusQuery(saved.getId(), saved.getTicketCode(), saved.getQueriedAt(), saved.getRequestedBy());
    }

    @Override public Optional<GateAssignment> findActiveBySessionGateAndCategory(Long sessionId, Long gateId, String ticketCategory) { return gateAssignmentRepository.findFirstBySessionIdAndGateIdAndTicketCategoryAndActiveTrue(sessionId, gateId, ticketCategory).map(this::toDomain); }
    @Override public boolean existsActiveConflict(Long sessionId, Long gateId, String ticketCategory) { return gateAssignmentRepository.existsBySessionIdAndGateIdAndTicketCategoryAndActiveTrue(sessionId, gateId, ticketCategory); }
    @Override public GateAssignment save(GateAssignment assignment) {
        GateAssignmentEntity entity = new GateAssignmentEntity();
        entity.setId(assignment.id()); entity.setSessionId(assignment.sessionId()); entity.setGateId(assignment.gateId()); entity.setTicketCategory(assignment.ticketCategory()); entity.setZone(assignment.zone()); entity.setActive(assignment.active());
        return toDomain(gateAssignmentRepository.save(entity));
    }
    @Override public void publish(GateAssignment assignment) { }

    private ReaderDevice toDomain(ReaderDeviceEntity e) { return new ReaderDevice(e.getId(), e.getGateId(), e.getAssignedZone(), e.isEnabled()); }
    private Ticket toDomain(TicketEntity e) { return new Ticket(e.getId(), e.getCode(), e.getStatus(), e.getCategory(), e.getAllowedZone(), e.getSessionId(), e.isUsed()); }
    private EventSession toDomain(EventSessionEntity e) { return new EventSession(e.getId(), e.getEventDate(), e.isActive(), e.getMaxCapacity(), e.getCurrentOccupancy()); }
    private EntryRecord toDomain(EntryRecordEntity e) { return new EntryRecord(e.getTicketId(), e.getEventId(), e.getGateId(), e.getAccessType(), e.getEnteredAt()); }
    private GateAssignment toDomain(GateAssignmentEntity e) { return new GateAssignment(e.getId(), e.getSessionId(), e.getGateId(), e.getTicketCategory(), e.getZone(), e.isActive()); }
    private AccessAttempt toDomain(AccessAttemptEntity e) { return new AccessAttempt(e.getId(), e.getTicketId(), e.getEnteredTicketCode(), e.getReaderId(), e.getGateId(), e.getSessionId(), e.getChannel(), e.getResult(), e.getErrorCode(), e.getAttemptedAt()); }
}
