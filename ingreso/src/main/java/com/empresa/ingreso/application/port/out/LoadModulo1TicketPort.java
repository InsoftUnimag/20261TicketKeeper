package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.Modulo1TicketSnapshot;
import java.util.Optional;

public interface LoadModulo1TicketPort {

    Optional<Modulo1TicketSnapshot> findById(String ticketId);
}
