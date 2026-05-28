package com.empresa.ingreso.infrastructure.integration.adapter;

import com.empresa.ingreso.application.port.out.Modulo1Port;
import com.empresa.ingreso.interfaces.api.dto.Modulo1EventoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Component
public class Modulo1Adapter implements Modulo1Port {

    private final RestTemplate restTemplate;

    @Value("${modules.modulo1.base-url}")
    private String modulo1BaseUrl;

    public Modulo1Adapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<Modulo1EventoDto> findActiveEventById(String eventId) {
        String url = UriComponentsBuilder.fromHttpUrl(modulo1BaseUrl)
                .path("/api/v1/eventos/{id}")
                .buildAndExpand(eventId)
                .toUriString();

        try {
            Modulo1EventoDto evento = restTemplate.getForObject(url, Modulo1EventoDto.class);
            if (evento != null && "ACTIVO".equals(evento.estado())) {
                return Optional.of(evento);
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
