package com.almighty.holamundo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaMundoController {

    /// Vincula el parámetro de la consulta (query param) con la variable local.
    @GetMapping("/saludo-final")
    public String saludar(@RequestParam(value = "nombreUsuario") String nombre) {
        return "Hola mundo " + nombre;
    }
}
