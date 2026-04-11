# Feature Specification: Registrar re-ingreso
**Created**: 23-02-2026

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Autorizar re-ingreso válido (Priority: P1)

Yo como encargado de control de acceso escaneo un ticket que ya fue utilizado previamente, pero que tiene permitido re-ingreso según la configuración del evento. El sistema valida la política de re-ingreso y, si está permitido, registra un nuevo acceso.

**Why this priority**:

Permite controlar salidas temporales (ej. fumar, ir al estacionamiento, etc.) sin comprometer la seguridad del evento. Es crítica cuando el evento habilita re-ingreso.

**Independent Test**: 

Escanear un ticket previamente ingresado que tenga bandera de “re-ingreso permitido”. Si el sistema registra el nuevo acceso y devuelve “Re-ingreso autorizado”, el MVP es funcional.

**Acceptance Scenarios**:

1. **Scenario**: Re-ingreso permitido correctamente
   - **Given** un ticket previamente ingresado
   - **And** el evento permite re-ingreso
   - **When** el encargado escanea el ticket nuevamente
   - **Then** el sistema registra el re-ingreso
   - **And** devuelve estado “Re-ingreso autorizado”
---

### User Story 2 - Rechazar re-ingreso no permitido (P)riority: P1

Yo como encargado escaneo un ticket previamente usado, pero el evento no permite re-ingreso.

**Why this priority**:
Evita vulnerabilidades de seguridad cuando el evento tiene política de ingreso único.

**Independent Test**:
Escanear un ticket ya ingresado en un evento con política “sin re-ingreso”. El sistema debe devolver “Re-ingreso no permitido”.

**Acceptance Scenarios**:

1. **Scenario**: Evento sin política de re-ingreso
   - **Given** Given un ticket con estado “ingresado”
   - **And** el evento no permite re-ingreso
   - **When** se escanea el ticket nuevamente
   - **Then** el sistema rechaza el intento
   - **And** registra el intento como fallido

---

### User Story 3 - Controlar límite de re-ingresos (P)riority: P1

Yo como encargado escaneo un ticket que ya ha sido re-ingresado múltiples veces, superando el límite permitido.

**Why this priority**:

Permite controlar abusos cuando existe límite máximo de re-ingresos.

**Independent Test**:

Configurar límite de 2 re-ingresos. Al tercer intento, el sistema debe rechazarlo.

**Acceptance Scenarios**:

1. **Scenario**: Límite de re-ingresos superado
   - **Given** un ticket con número máximo de re-ingresos alcanzado
   - **When** se escanea nuevamente
   - **Then** el sistema rechaza el intento
   - **And** devuelve código “Límite excedido”

---

### User Story 4 – Mostrar límite de reingresos (Priority: P2)

Como encargado de control de acceso quiero visualizar si el ticket ha alcanzado el límite de reingresos permitidos para evitar accesos indebidos al evento.

**Why this priority**:

Permite controlar el uso indebido de tickets duplicados cuando los eventos permiten reingreso.

**Independent Test**:

Cuando un ticket intenta ingresar nuevamente, el sistema verifica el número de reingresos registrados.

**Acceptance Scenario**

1. **Scenario**: Mostrar contador de reingresos
   - **Given** un ticket con reingresos previos
   - **When** el encargado escanea el ticket
   - **Then** el sistema muestra:
          Reingresos usados: 1
          Límite permitido: 2

---

### User Story 5 - Registrar salida para monitoreo (Priority: P1)

Como encargado de control de acceso quiero marcar el ticket con estado "salida" cuando la persona abandona el establecimiento, para mantener un monitoreo confiable de quienes estan fuera y habilitar el control de re-ingreso.

**Why this priority**:

Permite trazabilidad de egresos y evita inconsistencias en re-ingresos. Sin esta marca, el control de re-ingreso y el conteo de personas dentro/fuera se vuelve inexacto.

**Independent Test**:

Escanear un ticket valido en un punto de salida y verificar que el estado cambie a "salida" y quede registrado el evento con timestamp y lector.

**Acceptance Scenarios**:

1. **Scenario**: Registrar salida valida
   - **Given** un ticket con estado "ingresado"
   - **When** el encargado escanea el ticket en salida
   - **Then** el sistema registra la salida
   - **And** actualiza el estado del ticket a "salida"

2. **Scenario**: Rechazar salida sin ingreso previo
   - **Given** un ticket sin registro de ingreso
   - **When** se intenta registrar salida
   - **Then** el sistema rechaza la operacion
   - **And** devuelve estado "salida no permitida"

3. **Scenario**: Evitar doble salida
   - **Given** un ticket con estado "salida"
   - **When** se intenta registrar otra salida
   - **Then** el sistema rechaza el intento
   - **And** registra el intento como fallido

---

### Edge Cases

¿Qué pasa si el ticket no tiene registro previo de ingreso?
→ El sistema debe tratarlo como primer ingreso y redirigir al flujo normal.

¿Qué pasa si alguien con ticket duplicado entra en mi lugar cuando yo estoy afuera porque el evento permite re-ingreso?
→ El sistema no se hace responsable.

¿Qué pasa si el evento permite re-ingreso pero el ticket fue cancelado después del primer ingreso?
→ El sistema debe rechazar el intento por estado inválido.

¿Qué pasa si dos lectores intentan registrar el re-ingreso simultáneamente?
→ El sistema debe garantizar control de concurrencia y evitar duplicidad.

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements


-**FR-001**: System MUST El sistema debe verificar si el evento permite re-ingreso.

-**FR-002**: System MUST El sistema debe validar que el ticket tenga un ingreso previo registrado.

-**FR-003**: El sistema debe verificar el límite máximo de re-ingresos permitido.

-**FR-004**: System MUST El sistema debe registrar cada re-ingreso con timestamp y lector.

-**FR-005**: System MUST El sistema debe rechazar el intento cuando la política de re-ingreso no lo permita.

-**FR-006**: System MUST El sistema debe devolver un resultado estructurado indicando aprobación o rechazo.

-**FR-007**: El sistema debe garantizar control de concurrencia para evitar doble re-ingreso simultáneo.

### Key Entities 

**Ticket**:
   Representa la credencial de acceso.
   Atributos clave: id, estado, indicador de re-ingreso permitido, contador de re-ingresos.

**IntentoIngreso**:
    Registra cada ingreso o re-ingreso.
    Atributos: id, ticket_id, fecha_hora, lector_id, tipo (ingreso / re-ingreso), resultado.
    
**Evento**:
    Define reglas de acceso.
    Atributos: id, permite_reingreso (boolean), limite_reingresos.


## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

 -**SC-001**: 100% de los re-ingresos autorizados quedan registrados correctamente.
 
 -**SC-002**: 0 casos de re-ingreso no autorizado aceptado en pruebas de seguridad.

 -**SC-003**: Tiempo de respuesta menor a 2 segundos por validación.
 
 -**SC-004**: 100% de los rechazos muestran código de error coherente con la política configurada.


