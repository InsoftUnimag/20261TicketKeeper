package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.interfaces.api.dto.Modulo1EventoDto;
import java.util.Optional;

public interface Modulo1Port {
    Optional<Modulo1EventoDto> findActiveEventById(String eventId);
}
