# Feature Specification: Frontend de Operacion de Ingreso

**Updated**: 2026-05-23

## Context

El proyecto actual contiene un backend Spring Boot en `ingreso/` que ya expone la API operativa en `http://localhost:8083` y habilita CORS para `http://localhost:3000` y `http://localhost:5173`.

Todavia no existe una app frontend en el repo. El frontend a implementar debe vivir como app separada, hermana de `ingreso/`, para no mezclar el ciclo de Maven con el de Node/Vite.

La UI objetivo no es una landing page ni un portal administrativo completo. Debe ser una consola operativa para reemplazar el uso directo de Swagger en los flujos de ingreso.

## User Scenarios & Testing

### User Story 1 - Consultar estado de ticket (Priority: P1)

Como operador de acceso, quiero consultar el estado de un ticket para validar rapidamente si puede continuar en el flujo operativo.

**Why this priority**: permite validar conectividad, carga inicial de datos y visualizacion de errores antes de ejecutar acciones que mutan estado.

**Independent Test**: el frontend permite ingresar un `ticketCode`, consultar `GET /api/v1/tickets/{ticketCode}/status` y mostrar resultado o error funcional.

**Acceptance Scenarios**:

1. **Scenario**: consulta exitosa
   - **Given** un ticket existente
   - **When** el operador consulta su estado
   - **Then** el sistema muestra `ticketStatus`, `ticketCode`, `sessionId`, `gateId` y `entryAt`

2. **Scenario**: ticket inexistente
   - **Given** un ticket no registrado
   - **When** el operador consulta su estado
   - **Then** el sistema muestra `status = NOT_FOUND`, `message` y `errorCode`

---

### User Story 2 - Registrar ingreso inicial (Priority: P1)

Como operador de ingreso, quiero registrar un intento de entrada para saber si el ticket puede acceder al evento.

**Why this priority**: es el flujo principal del modulo y valida integracion con lector, puerta, sesion y reglas de negocio.

**Independent Test**: el frontend permite enviar `POST /api/v1/entry-attempts` con datos validos y mostrar si el intento fue aprobado o rechazado.

**Acceptance Scenarios**:

1. **Scenario**: ingreso aprobado
   - **Given** un ticket activo y datos consistentes
   - **When** el operador registra el intento
   - **Then** el frontend muestra `status = APPROVED` y `attemptId`

2. **Scenario**: ingreso rechazado por negocio
   - **Given** un ticket duplicado, bloqueado o con sesion incorrecta
   - **When** el operador registra el intento
   - **Then** el frontend muestra `status`, `message` y `errorCode`

3. **Scenario**: ticket inexistente en ingreso y no recuperable desde modulo externo
   - **Given** un ticket que no existe localmente ni puede resolverse
   - **When** el operador registra el intento
   - **Then** el frontend muestra `status = REJECTED`, `message` y `errorCode = TICKET_NO_ENCONTRADO`

---

### User Story 3 - Registrar salida y reingreso (Priority: P2)

Como operador de acceso, quiero registrar salidas y reingresos para mantener el control de circulacion del asistente.

**Why this priority**: completa el flujo operativo despues de que estado e ingreso inicial ya sean utilizables.

**Independent Test**: el frontend permite invocar `POST /api/v1/access-flow/exits` y `POST /api/v1/access-flow/re-entries` desde modulos separados.

**Acceptance Scenarios**:

1. **Scenario**: salida exitosa
   - **Given** un ticket en estado compatible con salida
   - **When** el operador registra la salida
   - **Then** el frontend muestra `status = APPROVED` o el valor funcional retornado, junto con `message`

2. **Scenario**: reingreso aprobado
   - **Given** un ticket en estado `EXITED`
   - **When** el operador registra el reingreso
   - **Then** el frontend muestra `status = APPROVED`, `reEntriesUsed` y `reEntryLimit`

3. **Scenario**: reingreso rechazado
   - **Given** un ticket sin permiso de reingreso
   - **When** el operador intenta registrarlo
   - **Then** el frontend muestra `message` y `errorCode`

---

### User Story 4 - Asignar puerta (Priority: P3)

Como operador de configuracion, quiero crear asignaciones de puerta por sesion y categoria para preparar la operacion.

**Why this priority**: sirve para pruebas operativas y configuracion, pero no bloquea el primer MVP si el seed ya trae asignaciones utiles.

**Independent Test**: el frontend permite enviar `POST /api/v1/gate-assignments` y reflejar respuesta de exito o conflicto.

**Acceptance Scenarios**:

1. **Scenario**: asignacion creada
   - **Given** una sesion valida y una combinacion no repetida
   - **When** el operador crea la asignacion
   - **Then** el frontend muestra `status`, `message` y `assignmentId`

2. **Scenario**: sesion inexistente o request invalido
   - **Given** un `sessionId` no registrado o datos incompletos
   - **When** el operador intenta crear la asignacion
   - **Then** el frontend muestra el error devuelto por la API

## Edge Cases

- El backend no esta disponible en `localhost:8083`.
- El backend responde `400` con error funcional usando shape `{ timestamp, message, errorCode }`.
- La consulta de ticket responde `200` incluso cuando el ticket no existe y el frontend debe tratar `status = NOT_FOUND` como resultado funcional no exitoso.
- Ingreso inicial puede responder `201` tanto para `APPROVED` como para `REJECTED`; la UI no debe decidir solo por codigo HTTP.
- `readerId` y `sessionId` pueden variar si el entorno no parte de una base limpia; la UI debe permitir editar presets facilmente.
- El operador repite una accion y la API responde conflicto, duplicado o regla de negocio incumplida.
- El operador usa tickets seed con zona o categoria que no coincide con la puerta seleccionada.

## Requirements

### Functional Requirements

- **FR-001**: El sistema MUST exponer una pantalla inicial operativa, no una landing page.
- **FR-002**: El sistema MUST vivir en una app web separada del backend, propuesta en `ticker1/frontend/`.
- **FR-003**: El sistema MUST centralizar la URL base del backend y usar `http://localhost:8083` como valor local por defecto.
- **FR-004**: El sistema MUST permitir consultar estado de ticket por `ticketCode`.
- **FR-005**: El sistema MUST permitir registrar intento de ingreso.
- **FR-006**: El sistema MUST permitir registrar salida.
- **FR-007**: El sistema MUST permitir registrar reingreso.
- **FR-008**: El sistema MUST permitir crear asignaciones de puerta.
- **FR-009**: El sistema MUST mostrar claramente `status`, `message`, `errorCode` y el payload completo recibido cuando aplique.
- **FR-010**: El sistema MUST distinguir visualmente entre resultado aprobado, rechazo funcional y error tecnico.
- **FR-011**: El sistema MUST permitir editar rapidamente presets de `ticketCode`, `readerId`, `gateId`, `sessionId` y `channel` sin recargar la pagina.
- **FR-012**: El sistema MUST mostrar estados de carga y error por operacion de forma independiente.
- **FR-013**: El sistema MUST incluir presets visibles basados en datos seed reales del backend, al menos para `TICKET-ACTIVE-NORTH`, `TICKET-ENTERED` y `TICKET-EXITED`.
- **FR-014**: El sistema SHOULD enviar el header opcional `X-Requested-By` en la consulta de estado para dejar trazabilidad del operador.
- **FR-015**: El sistema MUST normalizar en UI los distintos shapes de respuesta del backend sin modificar reglas de negocio del servidor.

### Key Entities

- **Ticket Query Form**: formulario para consultar un ticket por codigo y presentar su estado operativo.
- **Access Operation Form**: formulario reutilizable para ingreso, salida y reingreso con `ticketCode`, `readerId`, `gateId`, `sessionId` y `channel`.
- **Gate Assignment Form**: formulario para crear configuraciones de puerta.
- **Preset Panel**: bloque con ejemplos de tickets y parametros seed editables para pruebas rapidas.
- **API Result Panel**: panel que presenta respuesta exitosa, rechazo funcional o error tecnico en formato consistente.

## Seed Data Reference

El backend actual siembra, entre otros, estos tickets fijos:

- `TICKET-ACTIVE-NORTH`
- `TICKET-ACTIVE-SOUTH`
- `TICKET-ENTERED`
- `TICKET-EXITED`
- `TICKET-CANCELED`
- `TICKET-BLOCKED`
- `TICKET-SECOND-SESSION`

Tambien crea puertas `101`, `102`, `103` y zonas `NORTE`, `SUR`, `VIP`. El frontend debe usar estos valores como referencia visible, pero no asumir que `readerId` y `sessionId` siempre son constantes fuera de una base limpia.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Un operador puede consultar el estado de un ticket seed en menos de 30 segundos desde que abre la app.
- **SC-002**: Un operador puede ejecutar ingreso, salida y reingreso sin usar Swagger.
- **SC-003**: Cada operacion muestra respuesta o error sin depender de inspeccion manual del tab de red.
- **SC-004**: El frontend queda listo para demo local consumiendo el backend en `localhost:8083`.
- **SC-005**: La primera iteracion puede construirse sin cambios obligatorios en la API existente.
