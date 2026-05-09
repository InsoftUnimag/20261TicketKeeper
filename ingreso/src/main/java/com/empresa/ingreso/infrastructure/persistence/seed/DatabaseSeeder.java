package com.empresa.ingreso.infrastructure.persistence.seed;

import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.domain.model.AttemptResult;
import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.infrastructure.persistence.entity.AccessAttemptEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EventSessionEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.GateAssignmentEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataAccessAttemptRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEventSessionRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataGateAssignmentRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataReaderDeviceRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(value = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements ApplicationRunner {

    private final SpringDataEventSessionRepository eventSessionRepository;
    private final SpringDataReaderDeviceRepository readerDeviceRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataGateAssignmentRepository gateAssignmentRepository;
    private final SpringDataEntryRecordRepository entryRecordRepository;
    private final SpringDataAccessAttemptRepository accessAttemptRepository;

    public DatabaseSeeder(
            SpringDataEventSessionRepository eventSessionRepository,
            SpringDataReaderDeviceRepository readerDeviceRepository,
            SpringDataTicketRepository ticketRepository,
            SpringDataGateAssignmentRepository gateAssignmentRepository,
            SpringDataEntryRecordRepository entryRecordRepository,
            SpringDataAccessAttemptRepository accessAttemptRepository
    ) {
        this.eventSessionRepository = eventSessionRepository;
        this.readerDeviceRepository = readerDeviceRepository;
        this.ticketRepository = ticketRepository;
        this.gateAssignmentRepository = gateAssignmentRepository;
        this.entryRecordRepository = entryRecordRepository;
        this.accessAttemptRepository = accessAttemptRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (ticketRepository.count() > 0) {
            return;
        }

        EventSessionEntity activeSession = saveSession(LocalDate.of(2026, 5, 10), true, 5000, 1250);
        EventSessionEntity secondActiveSession = saveSession(LocalDate.of(2026, 5, 11), true, 3000, 420);
        saveSession(LocalDate.of(2026, 5, 12), false, 2500, 0);

        ReaderDeviceEntity northReader = saveReader(101L, "NORTE", true);
        ReaderDeviceEntity southReader = saveReader(102L, "SUR", true);
        saveReader(103L, "VIP", true);
        saveReader(104L, "NORTE", false);

        saveGateAssignment(activeSession.getId(), northReader.getGateId(), "GENERAL", "NORTE", true);
        saveGateAssignment(activeSession.getId(), southReader.getGateId(), "GENERAL", "SUR", true);
        saveGateAssignment(activeSession.getId(), 103L, "VIP", "VIP", true);
        saveGateAssignment(secondActiveSession.getId(), northReader.getGateId(), "GENERAL", "NORTE", true);

        TicketEntity activeNorthTicket = saveTicket("TICKET-ACTIVE-NORTH", TicketStatus.ACTIVE, "GENERAL", "NORTE", activeSession.getId(), false);
        TicketEntity activeSouthTicket = saveTicket("TICKET-ACTIVE-SOUTH", TicketStatus.ACTIVE, "GENERAL", "SUR", activeSession.getId(), false);
        TicketEntity enteredTicket = saveTicket("TICKET-ENTERED", TicketStatus.ENTERED, "GENERAL", "NORTE", activeSession.getId(), true);
        TicketEntity exitedTicket = saveTicket("TICKET-EXITED", TicketStatus.EXITED, "GENERAL", "NORTE", activeSession.getId(), true);
        saveTicket("TICKET-CANCELED", TicketStatus.CANCELED, "GENERAL", "SUR", activeSession.getId(), false);
        saveTicket("TICKET-BLOCKED", TicketStatus.BLOCKED, "VIP", "VIP", activeSession.getId(), false);
        saveTicket("TICKET-SECOND-SESSION", TicketStatus.ACTIVE, "GENERAL", "NORTE", secondActiveSession.getId(), false);

        saveEntryRecord(enteredTicket.getId(), activeSession.getId(), northReader.getGateId(), AccessType.ENTRY, OffsetDateTime.parse("2026-05-10T16:00:00Z"));
        saveEntryRecord(exitedTicket.getId(), activeSession.getId(), northReader.getGateId(), AccessType.ENTRY, OffsetDateTime.parse("2026-05-10T14:00:00Z"));
        saveEntryRecord(exitedTicket.getId(), activeSession.getId(), northReader.getGateId(), AccessType.EXIT, OffsetDateTime.parse("2026-05-10T18:30:00Z"));

        saveApprovedAttempt(activeNorthTicket.getId(), activeNorthTicket.getCode(), northReader.getId(), northReader.getGateId(), activeSession.getId(), OffsetDateTime.parse("2026-05-10T12:00:00Z"));
        saveApprovedAttempt(activeSouthTicket.getId(), activeSouthTicket.getCode(), southReader.getId(), southReader.getGateId(), activeSession.getId(), OffsetDateTime.parse("2026-05-10T12:05:00Z"));
    }

    private EventSessionEntity saveSession(LocalDate eventDate, boolean active, Integer maxCapacity, Integer currentOccupancy) {
        EventSessionEntity session = new EventSessionEntity();
        session.setEventDate(eventDate);
        session.setActive(active);
        session.setMaxCapacity(maxCapacity);
        session.setCurrentOccupancy(currentOccupancy);
        return eventSessionRepository.save(session);
    }

    private ReaderDeviceEntity saveReader(Long gateId, String zone, boolean enabled) {
        ReaderDeviceEntity reader = new ReaderDeviceEntity();
        reader.setGateId(gateId);
        reader.setAssignedZone(zone);
        reader.setEnabled(enabled);
        return readerDeviceRepository.save(reader);
    }

    private GateAssignmentEntity saveGateAssignment(Long sessionId, Long gateId, String category, String zone, boolean active) {
        GateAssignmentEntity assignment = new GateAssignmentEntity();
        assignment.setSessionId(sessionId);
        assignment.setGateId(gateId);
        assignment.setTicketCategory(category);
        assignment.setZone(zone);
        assignment.setActive(active);
        return gateAssignmentRepository.save(assignment);
    }

    private TicketEntity saveTicket(String code, TicketStatus status, String category, String allowedZone, Long sessionId, boolean used) {
        TicketEntity ticket = new TicketEntity();
        ticket.setCode(code);
        ticket.setStatus(status);
        ticket.setCategory(category);
        ticket.setAllowedZone(allowedZone);
        ticket.setSessionId(sessionId);
        ticket.setUsed(used);
        return ticketRepository.save(ticket);
    }

    private EntryRecordEntity saveEntryRecord(Long ticketId, Long eventId, Long gateId, AccessType accessType, OffsetDateTime enteredAt) {
        EntryRecordEntity entryRecord = new EntryRecordEntity();
        entryRecord.setTicketId(ticketId);
        entryRecord.setEventId(eventId);
        entryRecord.setGateId(gateId);
        entryRecord.setAccessType(accessType);
        entryRecord.setEnteredAt(enteredAt);
        return entryRecordRepository.save(entryRecord);
    }

    private AccessAttemptEntity saveApprovedAttempt(
            Long ticketId,
            String enteredTicketCode,
            Long readerId,
            Long gateId,
            Long sessionId,
            OffsetDateTime attemptedAt
    ) {
        AccessAttemptEntity attempt = new AccessAttemptEntity();
        attempt.setTicketId(ticketId);
        attempt.setEnteredTicketCode(enteredTicketCode);
        attempt.setReaderId(readerId);
        attempt.setGateId(gateId);
        attempt.setSessionId(sessionId);
        attempt.setChannel(com.empresa.ingreso.domain.model.AccessChannel.QR);
        attempt.setResult(AttemptResult.APPROVED);
        attempt.setErrorCode(null);
        attempt.setAttemptedAt(attemptedAt);
        return accessAttemptRepository.save(attempt);
    }
}
