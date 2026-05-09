package com.empresa.ingreso.infrastructure.persistence.adapter;

import com.empresa.ingreso.application.port.out.LoadTicketByCodePort;
import com.empresa.ingreso.application.port.out.LoadTicketPort;
import com.empresa.ingreso.application.port.out.SaveTicketPort;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TicketPersistenceAdapter implements LoadTicketPort, LoadTicketByCodePort, SaveTicketPort {

    private final SpringDataTicketRepository ticketRepository;

    public TicketPersistenceAdapter(SpringDataTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Optional<Ticket> findByCodeForUpdate(String code) {
        return ticketRepository.findByCodeForUpdate(code).map(this::toDomain);
    }

    @Override
    public Optional<Ticket> findByCode(String code) {
        return ticketRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = new TicketEntity();
        entity.setId(ticket.id());
        entity.setCode(ticket.code());
        entity.setStatus(ticket.status());
        entity.setCategory(ticket.category());
        entity.setAllowedZone(ticket.allowedZone());
        entity.setSessionId(ticket.sessionId());
        entity.setUsed(ticket.used());
        return toDomain(ticketRepository.save(entity));
    }

    private Ticket toDomain(TicketEntity entity) {
        return new Ticket(
                entity.getId(),
                entity.getCode(),
                entity.getStatus(),
                entity.getCategory(),
                entity.getAllowedZone(),
                entity.getSessionId(),
                entity.isUsed()
        );
    }
}
