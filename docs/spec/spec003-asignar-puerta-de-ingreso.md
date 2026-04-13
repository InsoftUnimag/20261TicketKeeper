# Feature Specification: [Asignar Puerta de Ingreso]

**Created**: 21-02-2026 

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

### User Story 1 - Distribución de Accesos por Categoría (Priority: P1)

Como Encargado de control de acceso, quiero vincular categorías de tickets específicos a puertas determinadas para distribuir el flujo de personas y minimizar los tiempos de espera.

**Why this priority**:

Es fundamental para la logística del evento; sin una asignación previa, el sistema no puede validar si un usuario intenta ingresar por la "Zona Incorrecta".

**Independent Test**: 

Se puede probar vinculando la "Zona A" exclusivamente a tickets de "Categoría VIP" y verificando que un ticket "General" sea rechazado en ese punto.

**Acceptance Scenarios**:

1. **Scenario**: Asignación exitosa de zona a puerta.
   - **Given** Una lista de puertas disponibles y las categorías de tickets vendidos.
   - **When**El encargado asigna la "Zona B" a la "Categoría General".
   - **Then** El sistema actualiza las reglas de validación para que solo los tickets "General" sean marcados como "Exitosos" en esa puerta.

---

### User Story 2 - Validar capacidad de flujo por puerta (Priority: P2)

Como Encargado de control de acceso, quiero verificar que la cantidad de tickets asignados a una puerta no supere su capacidad y poder modificar la asignación de puertas en tiempo real si una entrada presenta congestión, para redistribuir a los asistentes.

**Why this priority**:

Permite la flexibilidad operativa ante imprevistos en la capacidad de procesamiento de los puntos de entrada.

**Independent Test**:

Test: Cambiar la configuración de una puerta activa y confirmar que el cambio se refleja inmediatamente en los dispositivos de escaneo.

**Acceptance Scenarios**:

1. **Scenario**: Re-asignación de puerta por alta demanda.
   - **Given** La "Zona C" está saturada y la "Zona D" (sin asignación) está libre.
   - **When** El encargado habilita la "Zona D" para la misma categoría de la "Zona C".
   - **Then** El sistema permite el ingreso de esos tickets en ambos puntos de acceso.

---

### Edge Cases
¿Qué pasa si se intenta desasignar una puerta que ya tiene registros de ingreso? 
→ El sistema debe advertir que existen datos de ingreso vinculados, pero permitir la desasignación para evitar bloqueos operativos.

¿Cómo maneja el sistema una puerta asignada a múltiples categorías conflictivas? 
→El sistema debe validar que las categorías no se sobrepongan de forma que causen errores de "Zona Incorrecta" involuntarios 

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements


-**FR-001**: System MUST permitir visualizar el mapa de puertas y las categorías de tickets disponibles.

-**FR-002**: System MUST permitir la vinculación 1:N (una puerta a varias categorías) o N:1 (varias puertas a una categoría).

-**FR-003**: System MUST enviar las actualizaciones de asignación a los dispositivos de "Procesar intento de ingreso" de forma sincronizada.

-**FR-004**: System MUST permitir la consulta del flujo proyectado vs. el flujo real por puerta.

-**FR-005**: System MUST bloquear el ingreso en puertas que no tengan una asignación activa.

### Key Entities 

**Puerta**:
    Representa cada punto fisico de acceso.
    Atributos clave: id, nombre, capacidad, estado.

**Categoria de ticket**:
    Representa la categoria de acceso de cada ticket (General, VIP, etc.).
    Atributos: id,nombre, prioridad.


## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

 -**SC-001**: Un cambio en la asignación de puerta debe propagarse a todos los puntos de control en menos de 5 segundos.

 -**SC-002**: El sistema debe prevenir el 100% de los ingresos de "Zona Incorrecta" basados en la configuración de puertas vigente.

 -**SC-003**: El encargado debe poder completar una asignación masiva de puertas en menos de 3 minutos antes del inicio del evento.


