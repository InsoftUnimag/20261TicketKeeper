package com.empresa.ingreso.infrastructure.persistence.adapter;

import com.empresa.ingreso.application.port.out.SaveTicketStatusQueryPort;
import com.empresa.ingreso.domain.model.TicketStatusQuery;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketStatusQueryEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketStatusQueryRepository;
import org.springframework.stereotype.Component;

@Component
public class TicketStatusQueryPersistenceAdapter implements SaveTicketStatusQueryPort {

    private final SpringDataTicketStatusQueryRepository ticketStatusQueryRepository;

    public TicketStatusQueryPersistenceAdapter(SpringDataTicketStatusQueryRepository ticketStatusQueryRepository) {
        this.ticketStatusQueryRepository = ticketStatusQueryRepository;
    }

    @Override
    public TicketStatusQuery save(TicketStatusQuery query) {
        TicketStatusQueryEntity entity = new TicketStatusQueryEntity();
        entity.setId(query.id());
        entity.setTicketCode(query.ticketCode());
        entity.setQueriedAt(query.queriedAt());
        entity.setRequestedBy(query.requestedBy());
        return toDomain(ticketStatusQueryRepository.save(entity));
    }

    private TicketStatusQuery toDomain(TicketStatusQueryEntity entity) {
        return new TicketStatusQuery(
                entity.getId(),
                entity.getTicketCode(),
                entity.getQueriedAt(),
                entity.getRequestedBy()
        );
    }
}
