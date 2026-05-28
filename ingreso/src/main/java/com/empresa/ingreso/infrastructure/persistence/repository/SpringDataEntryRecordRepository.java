package com.empresa.ingreso.infrastructure.persistence.repository;

import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.infrastructure.persistence.dto.AttendingTicketInfo;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataEntryRecordRepository extends JpaRepository<EntryRecordEntity, Long> {

    Optional<EntryRecordEntity> findFirstByTicketIdOrderByEnteredAtDesc(Long ticketId);

    List<EntryRecordEntity> findByEventIdOrderByEnteredAtAsc(String eventId);

    long countByTicketIdAndAccessType(Long ticketId, AccessType accessType);

    @Query("SELECT count(DISTINCT e.ticketId) FROM EntryRecordEntity e WHERE e.eventId = :eventId AND e.accessType IN ('ENTRY', 'RE_ENTRY')")
    long countDistinctTicketsByEventId(@Param("eventId") String eventId);

    @Query("SELECT NEW com.empresa.ingreso.infrastructure.persistence.dto.AttendingTicketInfo(t.externalTicketId, MIN(e.enteredAt)) " +
           "FROM EntryRecordEntity e JOIN TicketEntity t ON e.ticketId = t.id " +
           "WHERE e.eventId = :eventId AND e.accessType IN ('ENTRY', 'RE_ENTRY') " +
           "GROUP BY t.externalTicketId")
    List<AttendingTicketInfo> findAttendingTicketsByEventId(@Param("eventId") String eventId);
}
