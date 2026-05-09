# Feature Specification: Frontend de Operacion de Ingreso

**Created**: 2026-05-09

## User Scenarios & Testing

### User Story 1 - Consultar estado de ticket (Priority: P1)

Como operador de acceso, quiero consultar el estado de un ticket para validar rapidamente si puede continuar en el flujo operativo.

**Why this priority**: es la consulta mas simple y permite validar conectividad, datos semilla y manejo de errores antes de ejecutar acciones que mutan estado.

**Independent Test**: el frontend debe permitir ingresar un `ticketCode`, consultar `GET /api/v1/tickets/{ticketCode}/status` y mostrar resultado o error sin depender de otras pantallas.

**Acceptance Scenarios**:

1. **Scenario**: consulta exitosa
   - **Given** un ticket existente
   - **When** el operador consulta su estado
   - **Then** el sistema muestra `ticketStatus`, `sessionId`, `gateId` y `entryAt`

2. **Scenario**: ticket inexistente
   - **Given** un ticket no registrado
   - **When** el operador consulta su estado
   - **Then** el sistema muestra el error funcional retornado por la API

---

### User Story 2 - Registrar ingreso inicial (Priority: P1)

Como operador de ingreso, quiero registrar un intento de entrada para saber si el ticket puede acceder al evento.

**Why this priority**: es el flujo principal del modulo y valida la integracion con lector, puerta, sesion y reglas de negocio.

**Independent Test**: el frontend debe permitir enviar `POST /api/v1/entry-attempts` con datos validos y mostrar si el intento fue aprobado o rechazado.

**Acceptance Scenarios**:

1. **Scenario**: ingreso aprobado
   - **Given** un ticket activo y datos consistentes
   - **When** el operador registra el intento
   - **Then** el frontend muestra estado `APPROVED` y el `attemptId`

2. **Scenario**: ingreso rechazado
   - **Given** un ticket duplicado, invalido o con sesion incorrecta
   - **When** el operador registra el intento
   - **Then** el frontend muestra `status`, `message` y `errorCode`

---

### User Story 3 - Registrar salida y reingreso (Priority: P2)

Como operador de acceso, quiero registrar salidas y reingresos para mantener el control de circulacion del asistente.

**Why this priority**: es parte del flujo completo, pero depende de que la validacion de estado y el ingreso inicial ya sean operables.

**Independent Test**: el frontend debe permitir invocar `POST /api/v1/access-flow/exits` y `POST /api/v1/access-flow/re-entries` desde formularios separados.

**Acceptance Scenarios**:

1. **Scenario**: salida exitosa
   - **Given** un ticket en estado compatible con salida
   - **When** el operador registra la salida
   - **Then** el frontend muestra confirmacion de operacion aprobada

2. **Scenario**: reingreso rechazado
   - **Given** un ticket sin permiso de reingreso
   - **When** el operador intenta registrarlo
   - **Then** el frontend muestra el `errorCode` devuelto por la API

---

### User Story 4 - Asignar puerta (Priority: P3)

Como operador de configuracion, quiero crear asignaciones de puerta por sesion y categoria para preparar la operacion.

**Why this priority**: es util para pruebas y administracion, pero no bloquea el primer MVP operativo si el seed ya trae configuracion.

**Independent Test**: el frontend debe enviar `POST /api/v1/gate-assignments` y reflejar respuesta de exito o conflicto.

**Acceptance Scenarios**:

1. **Scenario**: asignacion creada
   - **Given** una sesion valida y una combinacion no repetida
   - **When** el operador crea la asignacion
   - **Then** el frontend muestra `assignmentId` y mensaje de exito

2. **Scenario**: sesion inexistente
   - **Given** un `sessionId` no registrado
   - **When** el operador intenta crear la asignacion
   - **Then** el frontend muestra `EVENTO_NO_ENCONTRADO`

### Edge Cases

- El backend esta caido o no responde.
- El operador ejecuta una accion con datos semilla inexistentes.
- El usuario envia texto o numeros invalidos en campos requeridos.
- El backend responde `400` con error funcional y el frontend debe diferenciarlo de error tecnico.
- El operador repite una accion y la API responde conflicto o duplicado.

## Requirements

### Functional Requirements

- **FR-001**: El sistema MUST exponer una pantalla inicial operativa, no una landing page.
- **FR-002**: El sistema MUST permitir consultar estado de ticket por `ticketCode`.
- **FR-003**: El sistema MUST permitir registrar intento de ingreso.
- **FR-004**: El sistema MUST permitir registrar salida.
- **FR-005**: El sistema MUST permitir registrar reingreso.
- **FR-006**: El sistema MUST permitir crear asignaciones de puerta.
- **FR-007**: El sistema MUST mostrar claramente `status`, `message`, `errorCode` y payload de respuesta.
- **FR-008**: El sistema MUST permitir editar facilmente datos de prueba sin recargar la pagina.
- **FR-009**: El sistema MUST mostrar estados de carga y error por operacion.
- **FR-010**: El sistema MUST centralizar la URL base del backend para facilitar ejecucion local.

### Key Entities

- **Ticket Query Form**: formulario para consultar un ticket por codigo y presentar su estado operativo.
- **Access Operation Form**: formulario reutilizable para ingreso, salida y reingreso con `ticketCode`, `readerId`, `gateId`, `sessionId` y `channel`.
- **Gate Assignment Form**: formulario para crear configuraciones de puerta.
- **API Result Panel**: panel que presenta respuesta exitosa o error del backend.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Un operador puede consultar el estado de un ticket semilla en menos de 30 segundos desde que abre la app.
- **SC-002**: Un operador puede ejecutar ingreso, salida y reingreso sin usar Swagger.
- **SC-003**: Cada operacion muestra respuesta o error sin ambiguedad tecnica para el usuario.
- **SC-004**: El frontend queda listo para demo local consumiendo el backend en `localhost`.
