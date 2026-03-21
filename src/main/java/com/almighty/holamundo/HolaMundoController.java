package com.almighty.holamundo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaMundoController {

    // El RequestParam busca el nombre que escribiste en el cuadro de texto del HTML
    @GetMapping("/saludo-final")
    public String saludar(@RequestParam(value = "nombreUsuario") String nombre) {
        return "Hola mundo " + nombre;
    }
}