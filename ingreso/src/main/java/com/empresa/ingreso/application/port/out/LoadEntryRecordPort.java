package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.EntryRecord;
import java.util.List;
import java.util.Optional;

public interface LoadEntryRecordPort { Optional<EntryRecord> findLatestByTicketId(Long ticketId); List<EntryRecord> findByEventId(String eventId); long countReEntriesByTicketId(Long ticketId); }
