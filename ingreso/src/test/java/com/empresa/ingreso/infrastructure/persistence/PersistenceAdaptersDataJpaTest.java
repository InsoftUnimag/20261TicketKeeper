package com.empresa.ingreso.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.domain.model.GateAssignment;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.domain.model.TicketStatus;
import com.empresa.ingreso.infrastructure.persistence.adapter.EntryRecordPersistenceAdapter;
import com.empresa.ingreso.infrastructure.persistence.adapter.GateAssignmentPersistenceAdapter;
import com.empresa.ingreso.infrastructure.persistence.adapter.TicketPersistenceAdapter;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        EntryRecordPersistenceAdapter.class,
        GateAssignmentPersistenceAdapter.class,
        TicketPersistenceAdapter.class
})
class PersistenceAdaptersDataJpaTest {

    @Autowired
    private EntryRecordPersistenceAdapter entryRecordPersistenceAdapter;

    @Autowired
    private GateAssignmentPersistenceAdapter gateAssignmentPersistenceAdapter;

    @Autowired
    private TicketPersistenceAdapter ticketPersistenceAdapter;

    @Test
    void entry_record_round_trip_preserves_generated_id() {
        var saved = entryRecordPersistenceAdapter.save(new com.empresa.ingreso.domain.model.EntryRecord(
                null,
                10L,
                20L,
                30L,
                AccessType.ENTRY,
                OffsetDateTime.parse("2026-04-17T12:00:00Z")
        ));

        assertThat(saved.id()).isNotNull();

        var latest = entryRecordPersistenceAdapter.findLatestByTicketId(10L);

        assertThat(latest).isPresent();
        assertThat(latest.get().id()).isEqualTo(saved.id());
    }

    @Test
    void gate_assignment_detects_active_conflict() {
        gateAssignmentPersistenceAdapter.save(new GateAssignment(
                null,
                20L,
                5L,
                "VIP",
                "NORTE",
                true
        ));

        assertThat(gateAssignmentPersistenceAdapter.existsActiveConflict(20L, 5L, "VIP")).isTrue();
        assertThat(gateAssignmentPersistenceAdapter.findActiveBySessionGateAndCategory(20L, 5L, "VIP")).isPresent();
    }

    @Test
    void ticket_round_trip_supports_lookup_by_code() {
        var saved = ticketPersistenceAdapter.save(new Ticket(
                null,
                "T-100",
                TicketStatus.ACTIVE,
                "GEN",
                "A",
                77L,
                false
        ));

        assertThat(saved.id()).isNotNull();
        assertThat(ticketPersistenceAdapter.findByCode("T-100"))
                .isPresent()
                .get()
                .extracting(Ticket::status, Ticket::sessionId)
                .containsExactly(TicketStatus.ACTIVE, 77L);
    }
}
