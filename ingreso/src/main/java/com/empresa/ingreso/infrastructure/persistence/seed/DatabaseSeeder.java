package com.empresa.ingreso.infrastructure.persistence.seed;

import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.domain.model.AttemptResult;
import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.infrastructure.persistence.entity.AccessAttemptEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.GateAssignmentEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataAccessAttemptRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataGateAssignmentRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataReaderDeviceRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketRepository;
import java.time.OffsetDateTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(value = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements ApplicationRunner {

    private final SpringDataReaderDeviceRepository readerDeviceRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataGateAssignmentRepository gateAssignmentRepository;
    private final SpringDataEntryRecordRepository entryRecordRepository;
    private final SpringDataAccessAttemptRepository accessAttemptRepository;

    public DatabaseSeeder(
            SpringDataReaderDeviceRepository readerDeviceRepository,
            SpringDataTicketRepository ticketRepository,
            SpringDataGateAssignmentRepository gateAssignmentRepository,
            SpringDataEntryRecordRepository entryRecordRepository,
            SpringDataAccessAttemptRepository accessAttemptRepository
    ) {
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

        // Usamos UUIDs para simular los IDs de evento del Módulo 1
        String activeSessionId = "d5f9fd7e-3ac7-46eb-ba63-712e8e48abd3";
        String secondActiveSessionId = "a1b2c3d4-e5f6-7890-1234-567890abcdef";

        ReaderDeviceEntity northReader = saveReader(101L, "NORTE", true);
        ReaderDeviceEntity southReader = saveReader(102L, "SUR", true);
        ReaderDeviceEntity vipReader = saveReader(103L, "VIP", true);
        ReaderDeviceEntity traseraReader = saveReader(105L, "Trasera", true);
        saveReader(104L, "NORTE", false);

        saveGateAssignment(activeSessionId, northReader.getGateId(), "GENERAL", "NORTE");
        saveGateAssignment(activeSessionId, southReader.getGateId(), "GENERAL", "SUR");
        saveGateAssignment(activeSessionId, vipReader.getGateId(), "VIP", "VIP");
        saveGateAssignment(activeSessionId, traseraReader.getGateId(), "GENERAL", "Trasera");
        saveGateAssignment(secondActiveSessionId, northReader.getGateId(), "GENERAL", "NORTE");

        TicketEntity activeNorthTicket = saveTicket("TICKET-ACTIVE-NORTH", TicketStatus.ACTIVE, "GENERAL", "NORTE", activeSessionId, false);
        TicketEntity activeSouthTicket = saveTicket("TICKET-ACTIVE-SOUTH", TicketStatus.ACTIVE, "GENERAL", "SUR", activeSessionId, false);
        TicketEntity enteredTicket = saveTicket("TICKET-ENTERED", TicketStatus.ENTERED, "GENERAL", "NORTE", activeSessionId, true);
        TicketEntity exitedTicket = saveTicket("TICKET-EXITED", TicketStatus.EXITED, "GENERAL", "NORTE", activeSessionId, true);
        saveTicket("TICKET-CANCELED", TicketStatus.CANCELED, "GENERAL", "SUR", activeSessionId, false);
        saveTicket("TICKET-BLOCKED", TicketStatus.BLOCKED, "VIP", "VIP", activeSessionId, false);
        saveTicket("TICKET-SECOND-SESSION", TicketStatus.ACTIVE, "GENERAL", "NORTE", secondActiveSessionId, false);

        saveEntryRecord(enteredTicket.getId(), activeSessionId, northReader.getGateId(), AccessType.ENTRY, OffsetDateTime.parse("2026-05-10T16:00:00Z"));
        saveEntryRecord(exitedTicket.getId(), activeSessionId, northReader.getGateId(), AccessType.ENTRY, OffsetDateTime.parse("2026-05-10T14:00:00Z"));
        saveEntryRecord(exitedTicket.getId(), activeSessionId, northReader.getGateId(), AccessType.EXIT, OffsetDateTime.parse("2026-05-10T18:30:00Z"));

        saveApprovedAttempt(activeNorthTicket.getId(), activeNorthTicket.getCode(), northReader.getId(), northReader.getGateId(), activeSessionId, OffsetDateTime.parse("2026-05-10T12:00:00Z"));
        saveApprovedAttempt(activeSouthTicket.getId(), activeSouthTicket.getCode(), southReader.getId(), southReader.getGateId(), activeSessionId, OffsetDateTime.parse("2026-05-10T12:05:00Z"));
    }

    private ReaderDeviceEntity saveReader(Long gateId, String zone, boolean enabled) {
        ReaderDeviceEntity reader = new ReaderDeviceEntity();
        reader.setGateId(gateId);
        reader.setAssignedZone(zone);
        reader.setEnabled(enabled);
        return readerDeviceRepository.save(reader);
    }

    private void saveGateAssignment(String sessionId, Long gateId, String category, String zone) {
        GateAssignmentEntity assignment = new GateAssignmentEntity();
        assignment.setSessionId(sessionId);
        assignment.setGateId(gateId);
        assignment.setTicketCategory(category);
        assignment.setZone(zone);
        assignment.setActive(true);
        gateAssignmentRepository.save(assignment);
    }

    private TicketEntity saveTicket(String code, TicketStatus status, String category, String allowedZone, String sessionId, boolean used) {
        TicketEntity ticket = new TicketEntity();
        ticket.setCode(code);
        ticket.setStatus(status);
        ticket.setCategory(category);
        ticket.setAllowedZone(allowedZone);
        ticket.setSessionId(sessionId);
        ticket.setUsed(used);
        return ticketRepository.save(ticket);
    }

    private void saveEntryRecord(Long ticketId, String eventId, Long gateId, AccessType accessType, OffsetDateTime enteredAt) {
        EntryRecordEntity entryRecord = new EntryRecordEntity();
        entryRecord.setTicketId(ticketId);
        entryRecord.setEventId(eventId);
        entryRecord.setGateId(gateId);
        entryRecord.setAccessType(accessType);
        entryRecord.setEnteredAt(enteredAt);
        entryRecordRepository.save(entryRecord);
    }

    private void saveApprovedAttempt(
            Long ticketId,
            String enteredTicketCode,
            Long readerId,
            Long gateId,
            String sessionId,
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
        accessAttemptRepository.save(attempt);
    }
}
