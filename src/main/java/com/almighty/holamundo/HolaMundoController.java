package com.almighty.holamundo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HolaMundoController {

    @GetMapping("/saludo-final")
    public String saludar(@RequestParam(name = "nombreUsuario") String nombre, Model model) {
        model.addAttribute("usuarioFinal", nombre);
        return "saludo-final";
    }
}
