package com.empresa.ingreso.infrastructure.persistence.adapter;

import com.empresa.ingreso.application.port.out.SaveAccessAttemptPort;
import com.empresa.ingreso.domain.model.AccessAttempt;
import com.empresa.ingreso.infrastructure.persistence.entity.AccessAttemptEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataAccessAttemptRepository;
import org.springframework.stereotype.Component;

@Component
public class AccessAttemptPersistenceAdapter implements SaveAccessAttemptPort {

    private final SpringDataAccessAttemptRepository accessAttemptRepository;

    public AccessAttemptPersistenceAdapter(SpringDataAccessAttemptRepository accessAttemptRepository) {
        this.accessAttemptRepository = accessAttemptRepository;
    }

    @Override
    public AccessAttempt save(AccessAttempt accessAttempt) {
        AccessAttemptEntity entity = new AccessAttemptEntity();
        entity.setId(accessAttempt.id());
        entity.setTicketId(accessAttempt.ticketId());
        entity.setEnteredTicketCode(accessAttempt.enteredTicketCode());
        entity.setReaderId(accessAttempt.readerId());
        entity.setGateId(accessAttempt.gateId());
        entity.setSessionId(accessAttempt.sessionId());
        entity.setChannel(accessAttempt.channel());
        entity.setResult(accessAttempt.result());
        entity.setErrorCode(accessAttempt.errorCode());
        entity.setAttemptedAt(accessAttempt.attemptedAt());
        return toDomain(accessAttemptRepository.save(entity));
    }

    private AccessAttempt toDomain(AccessAttemptEntity entity) {
        return new AccessAttempt(
                entity.getId(),
                entity.getTicketId(),
                entity.getEnteredTicketCode(),
                entity.getReaderId(),
                entity.getGateId(),
                entity.getSessionId(),
                entity.getChannel(),
                entity.getResult(),
                entity.getErrorCode(),
                entity.getAttemptedAt()
        );
    }
}
