package com.empresa.ingreso.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.ingreso.application.dto.ProcessEntryAttemptRequest;
import com.empresa.ingreso.domain.model.AccessChannel;
import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.infrastructure.persistence.entity.AccessAttemptEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EventSessionEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataAccessAttemptRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEventSessionRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataReaderDeviceRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketRepository;
import com.empresa.ingreso.shared.errors.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultProcessEntryAttemptUseCaseTest {

    @Mock SpringDataReaderDeviceRepository readerRepo;
    @Mock SpringDataTicketRepository ticketRepo;
    @Mock SpringDataEventSessionRepository sessionRepo;
    @Mock SpringDataAccessAttemptRepository attemptRepo;
    @Mock SpringDataEntryRecordRepository entryRecordRepo;

    DefaultProcessEntryAttemptUseCase useCase;
    AtomicLong attemptIds;

    @BeforeEach
    void setUp() {
        attemptIds = new AtomicLong(100);
        when(attemptRepo.save(any())).thenAnswer(inv -> {
            AccessAttemptEntity a = inv.getArgument(0);
            if (a.getId() == null) a.setId(attemptIds.incrementAndGet());
            return a;
        });
        Clock fixed = Clock.fixed(Instant.parse("2026-04-17T12:00:00Z"), ZoneOffset.UTC);
        useCase = new DefaultProcessEntryAttemptUseCase(readerRepo, ticketRepo, sessionRepo, attemptRepo, entryRecordRepo, fixed);
    }

    @Test
    void rejects_when_reader_not_configured_and_does_not_query_ticket() {
        ProcessEntryAttemptRequest req = new ProcessEntryAttemptRequest("ABC", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findById(10L)).thenReturn(Optional.empty());

        var res = useCase.execute(req);

        assertThat(res.status()).isEqualTo("RECHAZADO");
        assertThat(res.errorCode()).isEqualTo(ErrorCode.LECTOR_NO_CONFIGURADO);
        assertThat(res.attemptId()).isNotNull();
        verify(ticketRepo, never()).findByCodeForUpdate(any());
    }

    @Test
    void rejects_when_ticket_not_found() {
        ProcessEntryAttemptRequest req = new ProcessEntryAttemptRequest("NOPE", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(ticketRepo.findByCodeForUpdate("NOPE")).thenReturn(Optional.empty());

        var res = useCase.execute(req);

        assertThat(res.status()).isEqualTo("RECHAZADO");
        assertThat(res.errorCode()).isEqualTo(ErrorCode.TICKET_NO_ENCONTRADO);
    }

    @Test
    void rejects_when_ticket_status_invalid() {
        ProcessEntryAttemptRequest req = new ProcessEntryAttemptRequest("T1", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(ticketRepo.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.CANCELED, 30L, "A", false)));

        var res = useCase.execute(req);

        assertThat(res.status()).isEqualTo("RECHAZADO");
        assertThat(res.errorCode()).isEqualTo(ErrorCode.ESTADO_INVALIDO);
    }

    @Test
    void rejects_when_session_invalid() {
        ProcessEntryAttemptRequest req = new ProcessEntryAttemptRequest("T1", 10L, 20L, 999L, AccessChannel.QR);
        when(readerRepo.findById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(ticketRepo.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.ACTIVE, 30L, "A", false)));

        var res = useCase.execute(req);

        assertThat(res.status()).isEqualTo("RECHAZADO");
        assertThat(res.errorCode()).isEqualTo(ErrorCode.SESION_INVALIDA);
    }

    @Test
    void rejects_when_zone_incorrect() {
        ProcessEntryAttemptRequest req = new ProcessEntryAttemptRequest("T1", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "B")));
        when(ticketRepo.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.ACTIVE, 30L, "A", false)));
        when(sessionRepo.findById(30L)).thenReturn(Optional.of(activeSession(30L)));

        var res = useCase.execute(req);

        assertThat(res.status()).isEqualTo("RECHAZADO");
        assertThat(res.errorCode()).isEqualTo(ErrorCode.ZONA_INCORRECTA);
    }

    @Test
    void rejects_when_ticket_duplicate() {
        ProcessEntryAttemptRequest req = new ProcessEntryAttemptRequest("T1", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(ticketRepo.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.ACTIVE, 30L, "A", true)));
        when(sessionRepo.findById(30L)).thenReturn(Optional.of(activeSession(30L)));

        var res = useCase.execute(req);

        assertThat(res.status()).isEqualTo("RECHAZADO");
        assertThat(res.errorCode()).isEqualTo(ErrorCode.TICKET_DUPLICADO);
    }

    @Test
    void approves_when_all_validations_pass() {
        ProcessEntryAttemptRequest req = new ProcessEntryAttemptRequest("T1", 10L, 20L, 30L, AccessChannel.MANUAL);
        when(readerRepo.findById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(ticketRepo.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.ACTIVE, 30L, "A", false)));
        when(sessionRepo.findById(30L)).thenReturn(Optional.of(activeSession(30L)));

        var res = useCase.execute(req);

        assertThat(res.status()).isEqualTo("APROBADO");
        assertThat(res.errorCode()).isNull();
        assertThat(res.attemptId()).isNotNull();
    }

    private static ReaderDeviceEntity validReader(Long readerId, Long gateId, String zone) {
        ReaderDeviceEntity r = new ReaderDeviceEntity();
        r.setId(readerId);
        r.setGateId(gateId);
        r.setAssignedZone(zone);
        r.setEnabled(true);
        return r;
    }

    private static TicketEntity ticket(Long id, String code, TicketStatus status, Long sessionId, String zone, boolean used) {
        TicketEntity t = new TicketEntity();
        t.setId(id);
        t.setCode(code);
        t.setStatus(status);
        t.setCategory("GEN");
        t.setAllowedZone(zone);
        t.setSessionId(sessionId);
        t.setUsed(used);
        return t;
    }

    private static EventSessionEntity activeSession(Long id) {
        EventSessionEntity s = new EventSessionEntity();
        s.setId(id);
        s.setActive(true);
        return s;
    }
}

