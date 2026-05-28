package com.empresa.ingreso.infrastructure.persistence.adapter;

import com.empresa.ingreso.application.port.out.LoadEntryRecordPort;
import com.empresa.ingreso.application.port.out.SaveEntryRecordPort;
import com.empresa.ingreso.domain.model.AccessType;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EntryRecordPersistenceAdapter implements LoadEntryRecordPort, SaveEntryRecordPort {

    private final SpringDataEntryRecordRepository entryRecordRepository;

    public EntryRecordPersistenceAdapter(SpringDataEntryRecordRepository entryRecordRepository) {
        this.entryRecordRepository = entryRecordRepository;
    }

    @Override
    public Optional<EntryRecord> findLatestByTicketId(Long ticketId) {
        return entryRecordRepository.findFirstByTicketIdOrderByEnteredAtDesc(ticketId).map(this::toDomain);
    }

    @Override
    public List<EntryRecord> findByEventId(String eventId) {
        return entryRecordRepository.findByEventIdOrderByEnteredAtAsc(eventId).stream().map(this::toDomain).toList();
    }

    @Override
    public long countReEntriesByTicketId(Long ticketId) {
        return entryRecordRepository.countByTicketIdAndAccessType(ticketId, AccessType.RE_ENTRY);
    }

    @Override
    public EntryRecord save(EntryRecord entryRecord) {
        EntryRecordEntity entity = new EntryRecordEntity();
        entity.setId(entryRecord.id());
        entity.setTicketId(entryRecord.ticketId());
        entity.setEventId(entryRecord.eventId());
        entity.setGateId(entryRecord.gateId());
        entity.setAccessType(entryRecord.accessType());
        entity.setEnteredAt(entryRecord.enteredAt());
        return toDomain(entryRecordRepository.save(entity));
    }

    private EntryRecord toDomain(EntryRecordEntity entity) {
        return new EntryRecord(
                entity.getId(),
                entity.getTicketId(),
                entity.getEventId(),
                entity.getGateId(),
                entity.getAccessType(),
                entity.getEnteredAt()
        );
    }
}
