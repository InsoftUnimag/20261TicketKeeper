package com.empresa.ingreso.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ingresoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Modulo de Ingreso y Control de Accesos")
                        .description("""
                                API del modulo de ingreso para validar accesos, registrar salidas y controlar reingresos.

                                Este modulo centraliza la operacion de tickets en molinetes, lectores y puertas del recinto.

                                ## Modelos de negocio

                                - **Ingreso inicial**: valida el primer acceso del ticket a la sesion.
                                - **Salida**: registra la salida del asistente del recinto.
                                - **Reingreso**: valida si el ticket puede volver a entrar segun su politica.

                                ## Flujo operativo

                                1. Configurar puertas con `POST /api/v1/gate-assignments`
                                2. Registrar ingreso con `POST /api/v1/entry-attempts`
                                3. Consultar estado con `GET /api/v1/tickets/{ticketCode}/status`
                                4. Registrar salida con `POST /api/v1/access-flow/exits`
                                5. Registrar reingreso con `POST /api/v1/access-flow/re-entries`

                                ## Error codes frecuentes

                                | Codigo | Significado |
                                | --- | --- |
                                | `TICKET_NO_ENCONTRADO` | El ticket no existe o no esta disponible |
                                | `TICKET_DUPLICADO` | El ticket ya fue utilizado para el mismo flujo |
                                | `REINGRESO_NO_PERMITIDO` | El ticket no puede volver a entrar |
                                | `LIMITE_REINGRESO_EXCEDIDO` | Ya alcanzo el maximo de reingresos |
                                | `SALIDA_NO_PERMITIDA` | La salida no aplica al estado actual |
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipo Ingreso")
                                .email("equipo.ingreso@empresa.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
