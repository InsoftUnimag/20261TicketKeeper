package com.empresa.ingreso.infrastructure.persistence.adapter;

import com.empresa.ingreso.application.port.out.LoadEventSessionPort;
import com.empresa.ingreso.domain.model.EventSession;
import com.empresa.ingreso.infrastructure.persistence.entity.EventSessionEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEventSessionRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EventSessionPersistenceAdapter implements LoadEventSessionPort {

    private final SpringDataEventSessionRepository eventSessionRepository;

    public EventSessionPersistenceAdapter(SpringDataEventSessionRepository eventSessionRepository) {
        this.eventSessionRepository = eventSessionRepository;
    }

    @Override
    public Optional<EventSession> findEventSessionById(Long sessionId) {
        return eventSessionRepository.findById(sessionId).map(this::toDomain);
    }

    private EventSession toDomain(EventSessionEntity entity) {
        return new EventSession(
                entity.getId(),
                entity.getEventDate(),
                entity.isActive(),
                entity.getMaxCapacity(),
                entity.getCurrentOccupancy()
        );
    }
}
