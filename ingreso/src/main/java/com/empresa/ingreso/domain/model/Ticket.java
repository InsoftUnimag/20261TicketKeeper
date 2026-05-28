package com.empresa.ingreso.domain.model;

public record Ticket(
        Long id,
        String code,
        TicketStatus status,
        String category,
        String allowedZone,
        String sessionId,
        boolean used,
        String externalTicketId,
        String externalEventId,
        String seatNumber,
        boolean reEntryAllowed
) {
    public Ticket(
            Long id,
            String code,
            TicketStatus status,
            String category,
            String allowedZone,
            String sessionId,
            boolean used
    ) {
        this(id, code, status, category, allowedZone, sessionId, used, null, null, null, false);
    }

    public boolean isInvalidForAccess() {
        return status != TicketStatus.ACTIVE && status != TicketStatus.ENTERED && status != TicketStatus.EXITED;
    }

    public boolean canRegisterReEntry() {
        return status == TicketStatus.EXITED && (externalTicketId == null || reEntryAllowed);
    }

    public boolean canRegisterExit() {
        return status == TicketStatus.ENTERED;
    }

    public Ticket markEntered() {
        return new Ticket(id, code, TicketStatus.ENTERED, category, allowedZone, sessionId, true, externalTicketId, externalEventId, seatNumber, reEntryAllowed);
    }

    public Ticket markExited() {
        return new Ticket(id, code, TicketStatus.EXITED, category, allowedZone, sessionId, true, externalTicketId, externalEventId, seatNumber, reEntryAllowed);
    }
}
