# Feature Specification: Informar estado del tickect

**Created**: 22/02/2026  

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

### User Story 1 - Consultar estado de un tickect válido (Priority: P1)

Yo como encargado de control de acceso quiero consultar el estado de un ticket 
para informar al asistente si es válido, usado, cancelado o inválido sin procesar ingreso.

**Why this priority**: Permite atención al público, resolución de dudas y soporte sin afectar el control de accesos. Es el caso base del feature.

**Independent Test**: Consultar un ticket existente y verificar que el sistema devuelve su estado actual sin modificar datos.

**Acceptance Scenarios**:

1. **Scenario**: Ticket activo y no usado
   - **Given** Un ticket existente con estado “activo” y sin registro de ingreso
   - **When** el encargado consulta el ticket
   - **Then** el sistema informa “Ticket válido – no utilizado

2. **Scenario**: Ticket ya utilizado
   - **Given** un ticket con registro previo de check-in
   - **When** se consulta el ticket
   - **Then** el sistema informa “Ticket ya utilizado” y muestra fecha/hora del ingreso

---

### User Story 2 - Informar ticket con estado inválido (Priority: P2)

Yo como encargado quiero saber si un ticket está cancelado, reembolsado o bloqueado para informar correctamente al asistente.

**Why this priority**: Reduce conflictos y evita intentos innecesarios de ingreso.

**Independent Test**: Consultar tickets con distintos estados inválidos y verificar la respuesta correcta.

**Acceptance Scenarios**:

1. **Scenario**: Ticket cancelado
   - **Given** un ticket con estado “cancelado”
   - **When** se consulta el ticket
   - **Then** el sistema informa “Ticket cancelado – ingreso no permitido”
   
2. **Scenario**: Ticket bloqueado
   - **Given** un ticket con estado “bloqueado”
   - **When** se consulta
   - **Then** el sistema informa “Ticket bloqueado”

---



### Edge Cases

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right edge cases.
-->

¿Qué pasa si el ticket existe pero tiene datos incompletos?
→ El sistema debe informar “Ticket con datos inconsistentes” y registrar el error.

¿Qué pasa si la base de datos no está disponible?
→ El sistema devuelve error técnico sin mostrar estado del ticket.

¿Qué pasa si el ticket fue usado hace segundos?
→ El sistema debe reflejar el estado actualizado (consistencia fuerte).

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

FR-001: System MUST permitir consultar un ticket por código único.

FR-002: System MUST validar la existencia del ticket.

FR-003: System MUST obtener y devolver el estado actual del ticket.

FR-004: System MUST indicar si el ticket fue usado previamente.

FR-005: System MUST informar sesión/evento asociado al ticket.

FR-006: System MUST NO modificar el estado del ticket durante la consulta.

FR-007: System MUST devolver un resultado estructurado con estado y mensaje descriptivo.

FR-008: System MUST registrar la consulta para auditoría (opcional según configuración).


### Key Entities *(include if feature involves data)*

Ticket
Representa la credencial de acceso.
Atributos clave: id, código, estado, sesión/evento, zona, indicador de usado.

ConsultaTicket
Representa una acción de consulta.
Atributos: id, ticket_id (nullable), fecha_hora, actor, resultado.

Sesión/Evento
Atributos: id, fecha, estado.

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

SC-001: 100% de las consultas devuelven un estado claro y comprensible.

SC-002: Tiempo de respuesta menor a 1 segundo en condiciones normales.

SC-003: 0% de consultas generan cambios en el estado del ticket.

SC-004: 100% de tickets inexistentes son detectados correctamente.

SC-005: El personal puede informar el estado del ticket sin necesidad de procesar ingreso.