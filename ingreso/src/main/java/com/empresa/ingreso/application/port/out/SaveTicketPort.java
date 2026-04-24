package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.Ticket;

public interface SaveTicketPort {

    Ticket save(Ticket ticket);
}
