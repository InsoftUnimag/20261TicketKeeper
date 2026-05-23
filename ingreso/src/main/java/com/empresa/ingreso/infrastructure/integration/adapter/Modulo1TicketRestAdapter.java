package com.empresa.ingreso.infrastructure.integration.adapter;

import com.empresa.ingreso.application.port.out.LoadModulo1TicketPort;
import com.empresa.ingreso.domain.model.Modulo1TicketSnapshot;
import com.empresa.ingreso.infrastructure.integration.dto.Modulo1TicketResponse;
import com.empresa.ingreso.shared.errors.TechnicalException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class Modulo1TicketRestAdapter implements LoadModulo1TicketPort {

    private final RestTemplate modulo1RestTemplate;

    public Modulo1TicketRestAdapter(RestTemplate modulo1RestTemplate) {
        this.modulo1RestTemplate = modulo1RestTemplate;
    }

    @Override
    public Optional<Modulo1TicketSnapshot> findById(String ticketId) {
        try {
            Modulo1TicketResponse response = modulo1RestTemplate.getForObject(
                    "/api/v1/tickets/{ticketId}",
                    Modulo1TicketResponse.class,
                    ticketId
            );
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(toSnapshot(response));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new TechnicalException("Error calling modulo 1 ticket endpoint", e);
        } catch (Exception e) {
            throw new TechnicalException("Error calling modulo 1 ticket endpoint", e);
        }
    }

    private Modulo1TicketSnapshot toSnapshot(Modulo1TicketResponse response) {
        return new Modulo1TicketSnapshot(
                response.ticketId(),
                response.eventoId(),
                response.estado(),
                response.categoria(),
                response.zona(),
                response.compuertaAsignada(),
                response.fechaEvento(),
                response.numeroAsiento(),
                response.permiteReingreso()
        );
    }
}
