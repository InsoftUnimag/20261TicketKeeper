package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.EventSession;
import java.util.Optional;

public interface LoadEventSessionPort {

    Optional<EventSession> findEventSessionById(Long sessionId);
}
