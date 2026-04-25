package com.empresa.ingreso.application.usecase;

import com.empresa.ingreso.application.port.in.GetEntryRecordByTicketResult;
import com.empresa.ingreso.application.port.in.GetEntryRecordByTicketUseCase;
import com.empresa.ingreso.application.port.out.LoadEntryRecordPort;
import com.empresa.ingreso.application.port.out.LoadTicketByCodePort;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.shared.errors.BusinessException;
import com.empresa.ingreso.shared.errors.ErrorCode;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultGetEntryRecordByTicketUseCase implements GetEntryRecordByTicketUseCase {
    private final LoadTicketByCodePort loadTicketByCodePort;
    private final LoadEntryRecordPort loadEntryRecordPort;

    public DefaultGetEntryRecordByTicketUseCase(LoadTicketByCodePort loadTicketByCodePort, LoadEntryRecordPort loadEntryRecordPort) {
        this.loadTicketByCodePort = loadTicketByCodePort; this.loadEntryRecordPort = loadEntryRecordPort;
    }

    @Override
    @Transactional(readOnly = true)
    public GetEntryRecordByTicketResult execute(String ticketCode) {
        Ticket ticket = loadTicketByCodePort.findByCode(ticketCode).orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NO_ENCONTRADO, "Ticket no encontrado"));
        Optional<EntryRecord> record = loadEntryRecordPort.findLatestByTicketId(ticket.id());
        if (record.isEmpty()) return new GetEntryRecordByTicketResult(ticket.code(), "Vendido - No asistio", ticket.sessionId(), null, null, null);
        EntryRecord r = record.get();
        return new GetEntryRecordByTicketResult(ticket.code(), "Validado", r.eventId(), r.gateId(), r.accessType(), r.enteredAt());
    }
}
