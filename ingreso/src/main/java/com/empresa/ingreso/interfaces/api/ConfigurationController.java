package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.interfaces.api.dto.Modulo1EventoDto;
import com.empresa.ingreso.interfaces.api.dto.Modulo1RecintoEstructuraDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ConfigurationController {

    private final RestTemplate restTemplate;

    @Value("${modules.modulo1.base-url}")
    private String modulo1BaseUrl;

    public ConfigurationController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/eventos")
    public ResponseEntity<List<Modulo1EventoDto>> getActiveEvents() {
        String url = UriComponentsBuilder.fromHttpUrl(modulo1BaseUrl)
                .path("/api/v1/eventos")
                .queryParam("estado", "ACTIVO")
                .toUriString();

        ResponseEntity<List<Modulo1EventoDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Modulo1EventoDto>>() {}
        );

        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/recintos/{id}/estructura")
    public ResponseEntity<Modulo1RecintoEstructuraDto> getRecintoEstructura(@PathVariable String id) {
        String url = UriComponentsBuilder.fromHttpUrl(modulo1BaseUrl)
                .path("/api/v1/recintos/{id}/estructura")
                .buildAndExpand(id)
                .toUriString();

        Modulo1RecintoEstructuraDto estructura = restTemplate.getForObject(url, Modulo1RecintoEstructuraDto.class);

        return ResponseEntity.ok(estructura);
    }
}
