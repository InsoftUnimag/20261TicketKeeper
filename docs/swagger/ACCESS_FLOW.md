# Flujo de acceso

## Conceptos

- `sessionId`: sesion o funcion a la que pertenece el ticket.
- `gateId`: puerta por la que se intenta operar el acceso.
- `readerId`: dispositivo o lector que reporta la operacion.
- `channel`: origen de la lectura. Actualmente `QR` o `MANUAL`.

## Flujo esperado

1. Configurar la operacion con `POST /api/v1/gate-assignments`.
2. Procesar el primer acceso con `POST /api/v1/entry-attempts`.
3. Consultar estado con `GET /api/v1/tickets/{ticketCode}/status`.
4. Registrar salida con `POST /api/v1/access-flow/exits`.
5. Registrar reingreso con `POST /api/v1/access-flow/re-entries` cuando aplique.

## Casos de error frecuentes

- `TICKET_NO_ENCONTRADO`: el codigo no existe.
- `TICKET_DUPLICADO`: el ticket ya fue usado para el flujo actual.
- `REINGRESO_NO_PERMITIDO`: el ticket no puede volver a entrar.
- `LIMITE_REINGRESO_EXCEDIDO`: ya supero la cantidad permitida.
- `SALIDA_NO_PERMITIDA`: la salida no es valida para el estado actual.
- `ASIGNACION_CONFLICTIVA`: la configuracion de puerta entra en conflicto con otra.
