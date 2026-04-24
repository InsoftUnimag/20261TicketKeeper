package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.EntryRecord;

public interface SaveEntryRecordPort {

    EntryRecord save(EntryRecord entryRecord);
}
