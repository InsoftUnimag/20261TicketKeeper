# Feature Specification: Consultar Registro de Ingreso

**Created**:23/02/2026

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

### User Story 1 - Consulta de registro por ticket (Priority: P1)

El módulo de liquidación de fondos consulta el registro de ingreso de un ticket específico para determinar si el asistente ingresó al evento.

**Why this priority**: 
Es crítico para el cálculo de liquidaciones, ya que los pagos dependen del estado final del ticket (validado o no asistió).
**Independent Test**: 
Puede probarse consultando un ticket previamente registrado como check-in y verificando que el sistema retorne el estado correcto sin depender de otras funcionalidades adicionales.

**Acceptance Scenarios**:

1. **Scenario**: Ticket con ingreso registrado
   - **Given** un ticket con check-in registrado
   - **When** el módulo de liquidación consulta el registro de ingreso
   - **Then** el sistema retorna estado “Validado”
   - **And** incluye fecha y hora de ingreso
   - **And** el tipo de acceso (Ingreso o Reingreso)

2. **Scenario**: Ticket sin ingreso registrado
   - **Given** un ticket vendido sin check-in registrado
   - **When** el módulo de liquidación consulta el registro
   - **Then** el sistema retorna estado “Vendido – No asistió”
---

### User Story 2 - Consulta de registros por evento (Priority: P2)

El módulo de liquidación consulta todos los registros de ingreso asociados a un evento para calcular el total de asistentes efectivos.

**Why this priority**: 
Permite realizar liquidaciones globales y generar reportes financieros del evento.

**Independent Test**: 
Puede probarse consultando los registros de un evento con múltiples tickets en distintos estados y validando que el total coincida con los registros almacenados.

**Acceptance Scenarios**:

1. **Scenario**: Consulta masiva de registros
   - **Given** un evento con múltiples registros de ingreso
   - **When** el módulo de liquidación solicita los registros del evento
   - **Then** el sistema retorna la lista completa de tickets con:
estado final del ticket
fecha y hora de ingreso (si aplica)
tipo de acceso (Ingreso o Reingreso)

---

### Edge Cases

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right edge cases.
-->

- ¿Qué pasa si se consulta un ticket inexistente?
  El sistema debe retornar mensaje “Ticket no encontrado” sin afectar otros registros.
-¿Qué pasa si no existen registros para el evento consultado?
  El sistema debe retornar lista vacía.
- ¿Qué pasa si hay inconsistencia entre estado del ticket y registro de ingreso?
  El sistema debe priorizar el registro de ingreso como fuente de verdad y registrar el incidente en logs
- ¿Qué pasa si el evento consultado no existe?
  El sistema debe retornar “Evento no encontrado”.


## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: El sistema debe permitir consultar el registro de ingreso de un ticket específico.
- **FR-002**: El sistema debe retornar el estado final del ticket (Validado / No se realizó check-in).
- **FR-003**: El sistema debe incluir fecha y hora de ingreso cuando exista check-in.
- **FR-004**: El sistema debe permitir consultar todos los registros de ingreso por evento.
- **FR-005**: El sistema debe retornar el tipo de acceso registrado (Ingreso o Reingreso).
- **FR-006**: El sistema debe registrar en logs las consultas realizadas por módulos externos.
- **FR-007**: El sistema debe responder a las consultas en tiempo real.

### Key Entities *(include if feature involves data)*

- **Registro de Ingreso:** Representa la confirmación de acceso al evento. Atributos clave: idTicket, idEvento, fechaHoraIngreso, puertaAsignada, estadoIngreso, tipoAcceso (Ingreso / Reingreso)
- **Ticket:** Representa el identificador único asociado a un asistente. Atributos: idTicket, codigoTicket, estado, categoria, eventoId, sesionId
- **Evento:** Entidad que agrupa múltiples tickets y registros de ingreso. Atributos: idEvento, nombreEvento, fechaEvento, estadoEvento

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: 100% de las consultas de tickets existentes retornan estado correcto.
- **SC-002**: Tiempo de respuesta promedio menor a 2 segundos por consulta.
- **SC-003**: 0 inconsistencias detectadas entre registro de ingreso y estado consultado en pruebas de integración.
- **SC-004**: El módulo de liquidación puede completar el cálculo financiero sin errores derivados de esta consulta.

