package com.empresa.ingreso.application.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class RecintoInfoService {

    // Simulación de un repositorio o servicio que conoce los recintos.
    // En un proyecto real, esto podría venir de una llamada a la API del Módulo 1 o de una tabla de configuración.
    private static final Map<String, String> RECINTO_NAMES = Map.of(
            "d5f9fd7e-3ac7-46eb-ba63-712e8e48abd3", "Edificio Principal",
            "f1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6", "Auditorio Anexo",
            "1", "Unimag" // Añadido el nuevo recinto
    );

    public Optional<String> getRecintoNameById(String recintoId) {
        if (recintoId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(RECINTO_NAMES.get(recintoId));
    }
}
