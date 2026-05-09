package com.empresa.ingreso.interfaces.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EventSessionEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.GateAssignmentEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEventSessionRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataGateAssignmentRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataReaderDeviceRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataReaderDeviceRepository readerDeviceRepository;

    @Autowired
    private SpringDataTicketRepository ticketRepository;

    @Autowired
    private SpringDataEventSessionRepository eventSessionRepository;

    @Autowired
    private SpringDataEntryRecordRepository entryRecordRepository;

    @Autowired
    private SpringDataGateAssignmentRepository gateAssignmentRepository;

    @Test
    void get_ticket_status_returns_not_found_for_unknown_ticket() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/UNKNOWN/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorCode").value("TICKET_NO_ENCONTRADO"));
    }

    @Test
    void process_entry_attempt_approves_valid_ticket() throws Exception {
        EventSessionEntity session = seedActiveSession();
        ReaderDeviceEntity reader = seedReader(20L, "A");
        seedTicket("T1", TicketStatus.ACTIVE, "A", session.getId(), false);

        mockMvc.perform(post("/api/v1/entry-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "ticketCode": "%s",
                                  "readerId": %d,
                                  "gateId": %d,
                                  "sessionId": %d,
                                  "channel": "QR"
                                }
                                """.formatted("T1", reader.getId(), reader.getGateId(), session.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.errorCode").isEmpty())
                .andExpect(jsonPath("$.attemptId").isNumber());
    }

    @Test
    void process_entry_attempt_creates_entry_record_visible_in_query_endpoint() throws Exception {
        EventSessionEntity session = seedActiveSession();
        ReaderDeviceEntity reader = seedReader(20L, "A");
        seedTicket("T2", TicketStatus.ACTIVE, "A", session.getId(), false);

        mockMvc.perform(post("/api/v1/entry-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "ticketCode": "%s",
                                  "readerId": %d,
                                  "gateId": %d,
                                  "sessionId": %d,
                                  "channel": "MANUAL"
                                }
                                """.formatted("T2", reader.getId(), reader.getGateId(), session.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/entry-records/tickets/T2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("T2"))
                .andExpect(jsonPath("$.finalStatus").value("Validado"))
                .andExpect(jsonPath("$.sessionId").value(session.getId()))
                .andExpect(jsonPath("$.gateId").value(20))
                .andExpect(jsonPath("$.accessType").value("ENTRY"));
    }

    @Test
    void get_entry_records_by_event_returns_records_for_existing_event() throws Exception {
        EventSessionEntity session = seedActiveSession();
        TicketEntity ticket = seedTicket("T-EVENT-1", TicketStatus.ENTERED, "A", session.getId(), true);
        seedEntryRecord(ticket.getId(), session.getId(), 20L, "ENTRY", OffsetDateTime.parse("2026-04-17T12:00:00Z"));

        mockMvc.perform(get("/api/v1/entry-records/events/" + session.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].finalStatus").value("Validado"))
                .andExpect(jsonPath("$[0].sessionId").value(session.getId()))
                .andExpect(jsonPath("$[0].gateId").value(20))
                .andExpect(jsonPath("$[0].accessType").value("ENTRY"));
    }

    @Test
    void assign_gate_creates_assignment_and_returns_created() throws Exception {
        EventSessionEntity session = seedActiveSession();

        mockMvc.perform(post("/api/v1/gate-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": %d,
                                  "gateId": 55,
                                  "ticketCategory": "GENERAL",
                                  "zone": "NORTE"
                                }
                                """.formatted(session.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignmentId").isNumber());
    }

    @Test
    void re_entry_approves_for_exited_ticket() throws Exception {
        EventSessionEntity session = seedActiveSession();
        ReaderDeviceEntity reader = seedReader(20L, "A");
        seedTicket("T-REENTRY", TicketStatus.EXITED, "A", session.getId(), true);

        mockMvc.perform(post("/api/v1/access-flow/re-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketCode": "T-REENTRY",
                                  "readerId": %d,
                                  "gateId": %d,
                                  "sessionId": %d,
                                  "channel": "QR"
                                }
                                """.formatted(reader.getId(), reader.getGateId(), session.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reEntriesUsed").value(1))
                .andExpect(jsonPath("$.reEntryLimit").value(2));
    }

    @Test
    void exit_approves_for_entered_ticket() throws Exception {
        EventSessionEntity session = seedActiveSession();
        ReaderDeviceEntity reader = seedReader(20L, "A");
        seedTicket("T-EXIT", TicketStatus.ENTERED, "A", session.getId(), true);

        mockMvc.perform(post("/api/v1/access-flow/exits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketCode": "T-EXIT",
                                  "readerId": %d,
                                  "gateId": %d,
                                  "sessionId": %d,
                                  "channel": "MANUAL"
                                }
                                """.formatted(reader.getId(), reader.getGateId(), session.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.errorCode").isEmpty());
    }

    private EventSessionEntity seedActiveSession() {
        EventSessionEntity session = new EventSessionEntity();
        session.setEventDate(java.time.LocalDate.of(2026, 4, 17));
        session.setActive(true);
        session.setMaxCapacity(100);
        session.setCurrentOccupancy(10);
        return eventSessionRepository.save(session);
    }

    private ReaderDeviceEntity seedReader(Long gateId, String zone) {
        ReaderDeviceEntity reader = new ReaderDeviceEntity();
        reader.setGateId(gateId);
        reader.setAssignedZone(zone);
        reader.setEnabled(true);
        return readerDeviceRepository.save(reader);
    }

    private TicketEntity seedTicket(String code, TicketStatus status, String zone, Long sessionId, boolean used) {
        TicketEntity ticket = new TicketEntity();
        ticket.setCode(code);
        ticket.setStatus(status);
        ticket.setCategory("GEN");
        ticket.setAllowedZone(zone);
        ticket.setSessionId(sessionId);
        ticket.setUsed(used);
        return ticketRepository.save(ticket);
    }

    private EntryRecordEntity seedEntryRecord(Long ticketId, Long eventId, Long gateId, String accessType, OffsetDateTime enteredAt) {
        EntryRecordEntity entryRecord = new EntryRecordEntity();
        entryRecord.setTicketId(ticketId);
        entryRecord.setEventId(eventId);
        entryRecord.setGateId(gateId);
        entryRecord.setAccessType(com.empresa.ingreso.domain.model.AccessType.valueOf(accessType));
        entryRecord.setEnteredAt(enteredAt);
        return entryRecordRepository.save(entryRecord);
    }

    private GateAssignmentEntity seedGateAssignment(Long sessionId, Long gateId, String ticketCategory, String zone, boolean active) {
        GateAssignmentEntity assignment = new GateAssignmentEntity();
        assignment.setSessionId(sessionId);
        assignment.setGateId(gateId);
        assignment.setTicketCategory(ticketCategory);
        assignment.setZone(zone);
        assignment.setActive(active);
        return gateAssignmentRepository.save(assignment);
    }
}
