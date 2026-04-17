# Feature Specification: Procesar intento de ingreso

**Created**: 21-02-2026

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Validacion y registro de ingreso exitoso (Priority: P1)

Como encargado de control de acceso, quiero registrar el check-in de un asistente escaneando su ticket para permitir su ingreso si es valido.

**Why this priority**: Es el nucleo de la operacion del evento; con esto hay control de acceso, trazabilidad y operacion continua en puertas.

**Independent Test**: Puede probarse escaneando un ticket valido previamente no utilizado y verificando que cambie de estado a `ingresado`, se registre hora, fecha y puerta, y que el sistema autorice el acceso.

**Acceptance Scenarios**:

1. **Scenario**: Ingreso exitoso por puerta correcta.
   - **Given** un ticket valido con estado `activo`
   - **And** el ticket pertenece a la sesion activa del evento
   - **And** la puerta corresponde a la zona permitida
   - **And** el ticket no ha sido previamente utilizado
   - **When** el encargado procesa el intento de ingreso
   - **Then** el sistema crea un `RegistroIngreso`
   - **And** guarda `idTicket`, `idEvento`, `fechaHoraIngreso` y `puertaAsignada`
   - **And** registra `tipoAcceso = INGRESO`
   - **And** actualiza el contador de ocupacion del evento o zona si ese modulo esta habilitado
   - **And** devuelve una respuesta estructurada con `status = APROBADO`

---

### User Story 2 - Rechazar intento por ticket duplicado (Priority: P1)

Como encargado, escaneo un ticket que ya fue procesado previamente en otro lector.

**Why this priority**: Evita fraude y reuso de credenciales. Es critico para la seguridad del evento.

**Independent Test**: Escanear dos veces el mismo ticket en lectores distintos. El segundo intento debe generar error `TICKET_DUPLICADO`.

**Acceptance Scenarios**:

1. **Scenario**: Ticket ya procesado
   - **Given** un ticket que ya tiene un registro previo de check-in
   - **When** el encargado escanea nuevamente el ticket
   - **Then** el sistema rechaza el intento
   - **And** devuelve error `TICKET_DUPLICADO`

---

### User Story 3 - Rechazar ingreso por zona incorrecta (Priority: P1)

Como encargado, escaneo un ticket valido pero en una puerta que no corresponde a su categoria o zona.

**Why this priority**: Controla la segmentacion de accesos.

**Independent Test**: Escanear un ticket valido en una puerta distinta a la asignada. Debe devolver error `ZONA_INCORRECTA`.

**Acceptance Scenarios**:

1. **Scenario**: Acceso no autorizado por zona
   - **Given** un ticket valido para zona A
   - **And** el lector pertenece a zona B
   - **When** se escanea el ticket
   - **Then** el sistema rechaza el intento
   - **And** devuelve error `ZONA_INCORRECTA`

---

### User Story 4 - Rechazar ingreso por estado invalido (Priority: P1)

Como encargado, escaneo un ticket cuyo estado no permite ingreso, por ejemplo `cancelado`, `reembolsado` o `bloqueado`.

**Why this priority**: Garantiza que solo tickets activos puedan ingresar.

**Independent Test**: Escanear un ticket con estado `cancelado`. El sistema debe devolver `ESTADO_INVALIDO`.

**Acceptance Scenarios**:

1. **Scenario**: Ticket con estado no valido
   - **Given** un ticket con estado distinto a `activo`
   - **When** se escanea el ticket
   - **Then** el sistema rechaza el intento
   - **And** devuelve error `ESTADO_INVALIDO`

---

### User Story 5 - Rechazar ingreso por sesion invalida (Priority: P1)

Como encargado, escaneo un ticket correspondiente a otro evento o fecha.

**Why this priority**: Evita ingresos fuera de la fecha o evento correspondiente.

**Independent Test**: Escanear un ticket de evento pasado o distinto. El sistema debe devolver `SESION_INVALIDA`.

**Acceptance Scenarios**:

1. **Scenario**: Ticket de evento distinto
   - **Given** un ticket asociado a una sesion diferente a la activa
   - **When** se escanea el ticket
   - **Then** el sistema rechaza el intento
   - **And** devuelve error `SESION_INVALIDA`

---

### User Story 6 - Asociar contexto del intento fallido (Priority: P1)

Como auditor del sistema, necesito que cada intento fallido tenga contexto suficiente para su analisis posterior.

**Why this priority**: Sin contexto, el registro no tiene valor operativo ni legal.

**Independent Test**: Revisar que cada intento fallido tenga la informacion minima obligatoria.

**Acceptance Scenarios**:

1. **Scenario**: Registro con datos completos
   - **Given** un intento de ingreso rechazado
   - **When** se registra el intento
   - **Then** el sistema almacena `fechaHora`, `lectorId`, `puertaId`, `sesionId`, `codigoTicketIngresado`, `resultado` y `codigoError`
   - **And** almacena `ticketId` solo si el ticket existe en el sistema

---

### User Story 7 - Registrar ingreso manual cuando el lector QR falla (Priority: P2)

Como encargado de control de acceso, quiero registrar manualmente el codigo de un ticket cuando el lector QR no este disponible, para permitir el ingreso del asistente sin detener la operacion del evento.

**Why this priority**: Los dispositivos de escaneo pueden fallar. El sistema debe permitir continuar la operacion mediante ingreso manual.

**Independent Test**: El encargado introduce manualmente el codigo del ticket, el sistema valida el ticket y registra el intento de ingreso igual que si hubiera sido escaneado.

**Acceptance Scenarios**:

1. **Scenario**: Registro manual exitoso
   - **Given** el lector QR no funciona
   - **When** el encargado ingresa manualmente el codigo del ticket
   - **Then** el sistema valida el ticket
   - **And** registra el intento de ingreso
   - **And** guarda `canalEntrada = MANUAL`

---

## Business Rules

### Orden de validacion

El sistema debe evaluar las reglas de negocio en el siguiente orden y devolver solo el primer error aplicable:

1. Validar configuracion del lector y puerta.
2. Validar existencia del ticket.
3. Validar estado del ticket.
4. Validar sesion activa del evento.
5. Validar zona o puerta autorizada.
6. Validar duplicidad de ingreso.

### Prioridad de errores

Si un ticket incumple varias reglas al mismo tiempo, el sistema debe responder con el error correspondiente a la primera validacion fallida segun el orden anterior.

### Regla de concurrencia

La validacion de duplicidad y el registro del ingreso exitoso deben ejecutarse dentro de la misma transaccion protegida por mecanismos de consistencia en base de datos para evitar doble check-in incluso con multiples lectores o multiples instancias de la aplicacion.

## Edge Cases

Que pasa si el ticket no existe en la informacion dada?
-> El sistema debe rechazar el intento y devolver error `TICKET_NO_ENCONTRADO`, registrando el intento como fallido con `ticketId = null`.

Que pasa si hay perdida de conexion con la base de datos?
-> El sistema debe devolver `ERROR_TECNICO` y no permitir el ingreso. El uso de una base de datos local queda fuera del alcance de este feature.

Que pasa si dos lectores procesan el mismo ticket exactamente al mismo tiempo?
-> El sistema debe garantizar atomicidad y evitar doble check-in mediante control de concurrencia a nivel transaccional y de persistencia.

Que pasa si el lector no tiene zona configurada?
-> El sistema debe rechazar el procesamiento y registrar error `LECTOR_NO_CONFIGURADO`.

Que pasa si falla el almacenamiento del intento fallido?
-> El sistema debe generar alerta tecnica y devolver `ERROR_TECNICO`. No debe continuar silenciosamente.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST validar la configuracion del lector antes de procesar el ticket.
- **FR-002**: System MUST validar la existencia del ticket en la base de datos.
- **FR-003**: System MUST validar que el estado del ticket sea valido para ingreso.
- **FR-004**: System MUST validar que la sesion del ticket coincida con la sesion activa del evento.
- **FR-005**: System MUST validar que la puerta pertenezca a la zona autorizada para el ticket.
- **FR-006**: System MUST verificar que el ticket no haya sido previamente utilizado.
- **FR-007**: System MUST registrar todo intento de ingreso, exitoso o fallido, con `fechaHora`, `lectorId`, `puertaId`, `sesionId`, `canalEntrada`, `resultado` y `codigoError` cuando corresponda.
- **FR-008**: System MUST permitir que `ticketId` sea nulo en intentos fallidos cuando el codigo ingresado no exista en el sistema.
- **FR-009**: System MUST devolver un resultado estructurado con `status`, `message`, `errorCode` opcional y datos minimos del intento procesado.
- **FR-010**: System MUST garantizar control de concurrencia para evitar doble procesamiento del mismo ticket incluso con multiples lectores o instancias.
- **FR-011**: System MUST crear un `RegistroIngreso` y actualizar el estado del ticket a `ingresado` cuando el intento sea exitoso.
- **FR-012**: System MUST almacenar el motivo de rechazo usando un diccionario de errores estable.
- **FR-013**: System MUST procesar el flujo manual reutilizando las mismas validaciones del flujo de escaneo y registrando `canalEntrada = MANUAL`.
- **FR-014**: System MUST actualizar el contador de ocupacion del evento o zona solo si dicho modulo existe y dentro de la misma transaccion del ingreso exitoso.
- **FR-015**: System MUST evaluar las validaciones en el orden de negocio definido en la seccion `Business Rules`.

### Key Entities

**Ticket**:
Representa la credencial de acceso.
Atributos clave: `id`, `codigoUnico`, `estado`, `categoria`, `zonaPermitida`, `sesionId`, `indicadorUsado`.

**IntentoIngreso**:
Representa cada intento de validacion de acceso.
Atributos: `id`, `ticketId` nullable, `codigoTicketIngresado`, `fechaHora`, `lectorId`, `puertaId`, `sesionId`, `canalEntrada`, `resultado`, `codigoError` nullable.

**RegistroIngreso**:
Representa la confirmacion de acceso al evento.
Atributos: `idTicket`, `idEvento`, `fechaHoraIngreso`, `puertaAsignada`, `tipoAcceso`.

**Lector**:
Representa el dispositivo o acceso fisico.
Atributos: `id`, `zonaAsignada`, `puertaId`, `estado`.

**SesionEvento**:
Representa la instancia temporal del evento.
Atributos: `id`, `fecha`, `estado`, `aforoMaximo`, `ocupacionActual`.

### Error Dictionary

- `LECTOR_NO_CONFIGURADO`
- `TICKET_NO_ENCONTRADO`
- `ESTADO_INVALIDO`
- `SESION_INVALIDA`
- `ZONA_INCORRECTA`
- `TICKET_DUPLICADO`
- `ERROR_TECNICO`

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% de los intentos de ingreso generan un registro auditable.
- **SC-002**: El sistema responde a un intento de escaneo en menos de 2 segundos en condiciones normales.
- **SC-003**: 0 casos de doble ingreso para un mismo ticket bajo condiciones de concurrencia.
- **SC-004**: 99% de los intentos validos son autorizados correctamente sin intervencion manual.
- **SC-005**: 100% de los rechazos muestran un codigo de error correspondiente al diccionario definido.
