package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.GetEntryRecordByTicketResult;
import com.empresa.ingreso.application.port.in.GetEntryRecordsByEventUseCase;
import com.empresa.ingreso.application.port.out.LoadEntryRecordPort;
import com.empresa.ingreso.application.port.out.LoadEventSessionPort;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.shared.errors.BusinessException;
import com.empresa.ingreso.shared.errors.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultGetEntryRecordsByEventUseCase implements GetEntryRecordsByEventUseCase {
    private final LoadEventSessionPort loadEventSessionPort;
    private final LoadEntryRecordPort loadEntryRecordPort;

    public DefaultGetEntryRecordsByEventUseCase(LoadEventSessionPort loadEventSessionPort, LoadEntryRecordPort loadEntryRecordPort) {
        this.loadEventSessionPort=loadEventSessionPort; this.loadEntryRecordPort=loadEntryRecordPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetEntryRecordByTicketResult> execute(Long eventId) {
        loadEventSessionPort.findEventSessionById(eventId).orElseThrow(() -> new BusinessException(ErrorCode.EVENTO_NO_ENCONTRADO, "Evento no encontrado"));
        return loadEntryRecordPort.findByEventId(eventId).stream().map(this::toResult).toList();
    }

    private GetEntryRecordByTicketResult toResult(EntryRecord record) {
        return new GetEntryRecordByTicketResult(null, "Validado", record.eventId(), record.gateId(), record.accessType(), record.enteredAt());
    }
}
