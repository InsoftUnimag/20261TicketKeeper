package com.empresa.ingreso.infrastructure.persistence.repository;

import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEntryRecordRepository extends JpaRepository<EntryRecordEntity, Long> {
    Optional<EntryRecordEntity> findFirstByTicketIdOrderByEnteredAtDesc(Long ticketId);
    List<EntryRecordEntity> findByEventIdOrderByEnteredAtAsc(Long eventId);
    long countByTicketIdAndAccessType(Long ticketId, AccessType accessType);
}
