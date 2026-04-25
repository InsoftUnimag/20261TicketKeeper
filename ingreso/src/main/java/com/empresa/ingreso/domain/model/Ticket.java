package com.empresa.ingreso.domain.model;

public record Ticket(
        Long id,
        String code,
        TicketStatus status,
        String category,
        String allowedZone,
        Long sessionId,
        boolean used
) {
    public boolean isInvalidForAccess() {
        return status != TicketStatus.ACTIVE && status != TicketStatus.ENTERED && status != TicketStatus.EXITED;
    }

    public boolean canRegisterReEntry() {
        return status == TicketStatus.EXITED;
    }

    public boolean canRegisterExit() {
        return status == TicketStatus.ENTERED;
    }

    public Ticket markEntered() {
        return new Ticket(id, code, TicketStatus.ENTERED, category, allowedZone, sessionId, true);
    }

    public Ticket markExited() {
        return new Ticket(id, code, TicketStatus.EXITED, category, allowedZone, sessionId, true);
    }
}
