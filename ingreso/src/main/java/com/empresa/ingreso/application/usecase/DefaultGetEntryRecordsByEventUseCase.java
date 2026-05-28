package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.GetEntryRecordByTicketResult;
import com.empresa.ingreso.application.port.in.GetEntryRecordsByEventUseCase;
import com.empresa.ingreso.application.port.out.LoadEntryRecordPort;
import com.empresa.ingreso.application.port.out.Modulo1Port;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.shared.errors.BusinessException;
import com.empresa.ingreso.shared.errors.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultGetEntryRecordsByEventUseCase implements GetEntryRecordsByEventUseCase {
    private final Modulo1Port modulo1Port;
    private final LoadEntryRecordPort loadEntryRecordPort;

    public DefaultGetEntryRecordsByEventUseCase(Modulo1Port modulo1Port, LoadEntryRecordPort loadEntryRecordPort) {
        this.modulo1Port = modulo1Port;
        this.loadEntryRecordPort = loadEntryRecordPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetEntryRecordByTicketResult> execute(String eventId) {
        // Validar que el evento existe en el Módulo 1
        modulo1Port.findActiveEventById(eventId).orElseThrow(() -> new BusinessException(ErrorCode.EVENTO_NO_ENCONTRADO, "Evento no encontrado"));
        
        // El puerto de carga de registros ya debería estar preparado para buscar por un String
        return loadEntryRecordPort.findByEventId(eventId).stream().map(this::toResult).toList();
    }

    private GetEntryRecordByTicketResult toResult(EntryRecord record) {
        // El ticketCode no está disponible en el EntryRecord, lo dejamos en null
        return new GetEntryRecordByTicketResult(null, "Validado", record.eventId(), record.gateId(), record.accessType(), record.enteredAt());
    }
}
