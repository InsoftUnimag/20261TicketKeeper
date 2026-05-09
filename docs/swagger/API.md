# API de Ingreso

## Swagger

- UI: `http://localhost:8080/swagger-ui.html`
- Spec JSON: `http://localhost:8080/v3/api-docs`

## Endpoints

### POST `/api/v1/entry-attempts`

Valida si un ticket puede ingresar y registra el intento.

Request:

```json
{
  "ticketCode": "TK-1001",
  "readerId": 15,
  "gateId": 7,
  "sessionId": 42,
  "channel": "QR"
}
```

Response `201`:

```json
{
  "status": "OK",
  "message": "Ingreso permitido",
  "errorCode": null,
  "attemptId": 125
}
```

### GET `/api/v1/entry-records/tickets/{ticketCode}`

Consulta el registro consolidado de acceso de un ticket.

### GET `/api/v1/entry-records/events/{eventId}`

Lista registros de acceso asociados a un evento.

### POST `/api/v1/gate-assignments`

Crea una asignacion entre sesion, puerta, categoria de ticket y zona.

Request:

```json
{
  "sessionId": 42,
  "gateId": 7,
  "ticketCategory": "GENERAL",
  "zone": "NORTE"
}
```

Response `201`:

```json
{
  "status": "OK",
  "message": "Asignacion creada",
  "assignmentId": 10
}
```

### POST `/api/v1/access-flow/re-entries`

Valida y registra un reingreso.

### POST `/api/v1/access-flow/exits`

Registra la salida de un ticket.

### GET `/api/v1/tickets/{ticketCode}/status`

Consulta el estado operativo del ticket. El header `X-Requested-By` es opcional.

## Tipos y valores relevantes

- `channel`: `QR`, `MANUAL`
- `accessType`: `ENTRY`, `RE_ENTRY`, `EXIT`
- `ticketStatus`: `ACTIVE`, `ENTERED`, `EXITED`, `CANCELED`, `REFUNDED`, `BLOCKED`

## Reglas de uso

- `entry-attempts` se usa para el primer ingreso valido.
- `exits` registra la salida del ticket del recinto.
- `re-entries` depende de que la politica de reingreso lo permita.
- `gate-assignments` debe existir correctamente para que la validacion operativa sea consistente.
