package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.Ticket;
import java.util.Optional;

public interface LoadTicketPort {

    Optional<Ticket> findByCodeForUpdate(String code);
}
