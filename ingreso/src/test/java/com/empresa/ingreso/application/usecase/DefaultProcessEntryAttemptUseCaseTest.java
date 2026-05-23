package com.empresa.ingreso.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import com.empresa.ingreso.application.port.in.ProcessEntryAttemptCommand;
import com.empresa.ingreso.application.port.out.LoadEventSessionPort;
import com.empresa.ingreso.application.port.out.LoadReaderDevicePort;
import com.empresa.ingreso.application.port.out.LoadTicketPort;
import com.empresa.ingreso.application.port.out.SaveAccessAttemptPort;
import com.empresa.ingreso.application.port.out.SaveEntryRecordPort;
import com.empresa.ingreso.application.port.out.SaveTicketPort;
import com.empresa.ingreso.domain.model.AccessAttempt;
import com.empresa.ingreso.domain.model.AccessChannel;
import com.empresa.ingreso.domain.model.EventSession;
import com.empresa.ingreso.domain.model.ReaderDevice;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.domain.model.TicketStatus;
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

    @Mock LoadReaderDevicePort readerRepo;
    @Mock LoadTicketPort loadTicketPort;
    @Mock SaveTicketPort saveTicketPort;
    @Mock LoadEventSessionPort sessionRepo;
    @Mock SaveAccessAttemptPort attemptRepo;
    @Mock SaveEntryRecordPort entryRecordRepo;

    DefaultProcessEntryAttemptUseCase useCase;
    AtomicLong attemptIds;

    @BeforeEach
    void setUp() {
        attemptIds = new AtomicLong(100);
        lenient().when(attemptRepo.save(any())).thenAnswer(inv -> {
            AccessAttempt attempt = inv.getArgument(0);
            return new AccessAttempt(
                    attempt.id() == null ? attemptIds.incrementAndGet() : attempt.id(),
                    attempt.ticketId(),
                    attempt.enteredTicketCode(),
                    attempt.readerId(),
                    attempt.gateId(),
                    attempt.sessionId(),
                    attempt.channel(),
                    attempt.result(),
                    attempt.errorCode(),
                    attempt.attemptedAt()
            );
        });
        lenient().when(saveTicketPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(entryRecordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Clock fixed = Clock.fixed(Instant.parse("2026-04-17T12:00:00Z"), ZoneOffset.UTC);
        useCase = new DefaultProcessEntryAttemptUseCase(
                readerRepo,
                loadTicketPort,
                saveTicketPort,
                sessionRepo,
                attemptRepo,
                entryRecordRepo,
                fixed
        );
    }

    @Test
    void rejects_when_reader_not_configured_and_does_not_query_ticket() {
        ProcessEntryAttemptCommand req = new ProcessEntryAttemptCommand("ABC", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findReaderDeviceById(10L)).thenReturn(Optional.empty());

        var res = useCase.execute(req);

        assertThat(res).isPresent();
        assertThat(res.get().status()).isEqualTo("REJECTED");
        assertThat(res.get().errorCode()).isEqualTo(ErrorCode.LECTOR_NO_CONFIGURADO);
        assertThat(res.get().attemptId()).isNotNull();
        verify(loadTicketPort, never()).findByCodeForUpdate(any());
    }

    @Test
    void rejects_when_ticket_not_found() {
        ProcessEntryAttemptCommand req = new ProcessEntryAttemptCommand("NOPE", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findReaderDeviceById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(loadTicketPort.findByCodeForUpdate("NOPE")).thenReturn(Optional.empty());

        var res = useCase.execute(req);

        assertThat(res).isEmpty();
    }

    @Test
    void rejects_when_ticket_status_invalid() {
        ProcessEntryAttemptCommand req = new ProcessEntryAttemptCommand("T1", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findReaderDeviceById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(loadTicketPort.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.CANCELED, 30L, "A", false)));

        var res = useCase.execute(req);

        assertThat(res).isPresent();
        assertThat(res.get().status()).isEqualTo("REJECTED");
        assertThat(res.get().errorCode()).isEqualTo(ErrorCode.ESTADO_INVALIDO);
    }

    @Test
    void rejects_when_session_invalid() {
        ProcessEntryAttemptCommand req = new ProcessEntryAttemptCommand("T1", 10L, 20L, 999L, AccessChannel.QR);
        when(readerRepo.findReaderDeviceById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(loadTicketPort.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.ACTIVE, 30L, "A", false)));

        var res = useCase.execute(req);

        assertThat(res).isPresent();
        assertThat(res.get().status()).isEqualTo("REJECTED");
        assertThat(res.get().errorCode()).isEqualTo(ErrorCode.SESION_INVALIDA);
    }

    @Test
    void rejects_when_zone_incorrect() {
        ProcessEntryAttemptCommand req = new ProcessEntryAttemptCommand("T1", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findReaderDeviceById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "B")));
        when(loadTicketPort.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.ACTIVE, 30L, "A", false)));
        when(sessionRepo.findEventSessionById(30L)).thenReturn(Optional.of(activeSession(30L)));

        var res = useCase.execute(req);

        assertThat(res).isPresent();
        assertThat(res.get().status()).isEqualTo("REJECTED");
        assertThat(res.get().errorCode()).isEqualTo(ErrorCode.ZONA_INCORRECTA);
    }

    @Test
    void rejects_when_ticket_duplicate() {
        ProcessEntryAttemptCommand req = new ProcessEntryAttemptCommand("T1", 10L, 20L, 30L, AccessChannel.QR);
        when(readerRepo.findReaderDeviceById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(loadTicketPort.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.ENTERED, 30L, "A", true)));

        var res = useCase.execute(req);

        assertThat(res).isPresent();
        assertThat(res.get().status()).isEqualTo("REJECTED");
        assertThat(res.get().errorCode()).isEqualTo(ErrorCode.TICKET_DUPLICADO);
    }

    @Test
    void approves_when_all_validations_pass() {
        ProcessEntryAttemptCommand req = new ProcessEntryAttemptCommand("T1", 10L, 20L, 30L, AccessChannel.MANUAL);
        when(readerRepo.findReaderDeviceById(10L)).thenReturn(Optional.of(validReader(10L, 20L, "A")));
        when(loadTicketPort.findByCodeForUpdate("T1")).thenReturn(Optional.of(ticket(1L, "T1", TicketStatus.ACTIVE, 30L, "A", false)));
        when(sessionRepo.findEventSessionById(30L)).thenReturn(Optional.of(activeSession(30L)));

        var res = useCase.execute(req);

        assertThat(res).isPresent();
        assertThat(res.get().status()).isEqualTo("APPROVED");
        assertThat(res.get().errorCode()).isNull();
        assertThat(res.get().attemptId()).isNotNull();
    }

    private static ReaderDevice validReader(Long readerId, Long gateId, String zone) {
        return new ReaderDevice(readerId, gateId, zone, true);
    }

    private static Ticket ticket(Long id, String code, TicketStatus status, Long sessionId, String zone, boolean used) {
        return new Ticket(id, code, status, "GEN", zone, sessionId, used);
    }

    private static EventSession activeSession(Long id) {
        return new EventSession(id, java.time.LocalDate.of(2026, 4, 17), true, 100, 10);
    }
}

