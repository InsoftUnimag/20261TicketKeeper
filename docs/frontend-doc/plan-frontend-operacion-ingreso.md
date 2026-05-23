# Implementation Plan: Frontend de Operacion de Ingreso

**Updated**: 2026-05-23
**Spec**: `docs/frontend-doc/spec-frontend-operacion-ingreso.md`

---

## 1. Summary

Se implementara un frontend operativo dentro del repo para consumir la API ya disponible en `ingreso/`. El objetivo es reemplazar el uso manual de Swagger en los flujos principales de ingreso con una consola unica, utilitaria y rapida de operar.

La app frontend no existe aun. La propuesta alineada al estado actual del proyecto es crear `ticker1/frontend/` como app React + Vite + TypeScript, separada del backend Spring Boot.

---

## 2. Technical Context

**Backend existente**: Spring Boot 3.5.x en `ingreso/`  
**Language/Version**: TypeScript 5.x  
**Primary Dependencies**: React 18, Vite 5, `fetch` nativo, opcionalmente `zod` solo para parsing defensivo del lado UI  
**Storage**: sin persistencia propia en frontend; solo estado en memoria  
**Testing**: Vitest + React Testing Library para componentes criticos; smoke manual contra backend local  
**Target Platform**: navegador web en entorno local  
**Project Type**: SPA separada, hermana del backend  
**Performance Goals**: interfaz lista para operar en una sola pantalla, sin navegacion profunda  
**Constraints**:
- backend por defecto en `http://localhost:8083`
- CORS habilitado para `http://localhost:3000` y `http://localhost:5173`
- la API mezcla respuestas funcionales con shapes distintos segun endpoint y tipo de error
- no duplicar reglas de negocio del backend

---

## 3. Current-State Findings

Hallazgos que obligan a ajustar la documentacion original:

1. El repo actual solo contiene el backend `ingreso/`; no hay `frontend/` creado todavia.
2. El puerto local real del backend es `8083`, no un valor implicito generico.
3. `GET /api/v1/tickets/{ticketCode}/status` devuelve `200` incluso para ticket inexistente, con `status = NOT_FOUND`.
4. `POST /api/v1/entry-attempts` devuelve `201` tanto para aprobacion como para rechazo funcional; el frontend debe leer el payload, no solo el HTTP status.
5. Los errores de `BusinessException` y validacion salen con shape `{ timestamp, message, errorCode }`, diferente al shape de las respuestas felices.
6. Existen datos seed utiles para presets: tickets fijos y puertas `101/102/103`, pero `sessionId` y `readerId` pueden depender de IDs autogenerados.

---

## 4. Architecture Approach

SPA React/Vite con una sola vista principal tipo dashboard operativo. La pantalla inicial mostrara modulos paralelos para:

- consulta de ticket
- ingreso inicial
- salida
- reingreso
- asignacion de puerta

Capas sugeridas:

- `src/app/`: bootstrap, layout y estilos globales
- `src/features/access/`: formularios de ingreso, salida y reingreso
- `src/features/ticket-status/`: consulta de estado
- `src/features/gate-assignment/`: asignacion de puerta
- `src/shared/api/`: cliente HTTP, normalizacion de respuestas y configuracion de base URL
- `src/shared/ui/`: paneles, campos, botones y badges reutilizables
- `src/shared/types/`: tipos del backend y view models

Decisiones:

- usar `fetch` y una capa pequena de normalizacion para mantener bajo el acoplamiento
- no introducir router en la primera iteracion; una sola pantalla es suficiente
- no derivar reglas de aprobacion desde el frontend; solo renderizar lo retornado por la API

---

## 5. Proposed Project Structure

```text
ticker1/
|-- docs/
|   `-- frontend-doc/
|       |-- plan-frontend-operacion-ingreso.md
|       `-- spec-frontend-operacion-ingreso.md
|-- frontend/
|   |-- package.json
|   |-- vite.config.ts
|   |-- tsconfig.json
|   |-- .env.example
|   `-- src/
|       |-- app/
|       |-- features/
|       |-- shared/
|       `-- main.tsx
`-- ingreso/
    |-- pom.xml
    `-- src/
```

**Structure Decision**: mantener `frontend/` como sibling de `ingreso/` evita mezclar Maven y Vite, y ya encaja con la configuracion CORS existente.

---

## 6. API Contracts To Reflect In UI

### Ticket status

- `GET /api/v1/tickets/{ticketCode}/status`
- payload exitoso o funcional:
  - `status`
  - `message`
  - `errorCode`
  - `ticketStatus`
  - `ticketCode`
  - `sessionId`
  - `gateId`
  - `entryAt`

### Entry attempt

- `POST /api/v1/entry-attempts`
- request:
  - `ticketCode`
  - `readerId`
  - `gateId`
  - `sessionId`
  - `channel: QR | MANUAL`
- response:
  - `status`
  - `message`
  - `errorCode`
  - `attemptId`

### Exit

- `POST /api/v1/access-flow/exits`
- request igual a entry attempt
- response:
  - `status`
  - `message`
  - `errorCode`

### Re-entry

- `POST /api/v1/access-flow/re-entries`
- request igual a entry attempt
- response:
  - `status`
  - `message`
  - `errorCode`
  - `reEntriesUsed`
  - `reEntryLimit`

### Gate assignment

- `POST /api/v1/gate-assignments`
- request:
  - `sessionId`
  - `gateId`
  - `ticketCategory`
  - `zone`
- response:
  - `status`
  - `message`
  - `assignmentId`

### Error normalization required

La UI debe convertir estas variantes a un modelo comun para render:

- respuesta funcional del endpoint
- rechazo funcional con `status` en payload
- error de validacion o negocio con `{ timestamp, message, errorCode }`
- error tecnico de red o `500`

---

## 7. Delivery Phases

## Phase 1 - Bootstrap

- Crear `frontend/` con Vite + React + TypeScript
- Configurar `.env.example` con `VITE_API_BASE_URL=http://localhost:8083`
- Definir scripts base: `dev`, `build`, `preview`, `test`
- Agregar `README.md` del frontend con arranque local junto al backend

## Phase 2 - Foundations

- Implementar cliente HTTP y helper `parseApiResult`
- Tipar DTOs alineados a los controllers actuales
- Crear layout principal de consola operativa
- Crear `ResultPanel` reutilizable con estados `success`, `functional-error`, `technical-error`, `loading`
- Crear `PresetPanel` con tickets y valores seed editables

## Phase 3 - User Story 1

- Implementar modulo `TicketStatusCard`
- Soportar header opcional `X-Requested-By`
- Mostrar campos de contexto y respuesta cruda expandible

## Phase 4 - User Story 2

- Implementar `EntryAttemptCard`
- Reutilizar formulario base para `ticketCode`, `readerId`, `gateId`, `sessionId`, `channel`
- Mostrar claramente que `201` no implica aprobacion si `status` llega rechazado

## Phase 5 - User Story 3

- Implementar `ExitCard`
- Implementar `ReEntryCard`
- Reutilizar el formulario base con presets para `TICKET-ENTERED` y `TICKET-EXITED`

## Phase 6 - User Story 4

- Implementar `GateAssignmentCard`
- Agregar ayudas visuales para categoria y zona
- Mostrar `assignmentId` y conflictos de forma consistente

## Phase 7 - Verification and Polish

- Probar manualmente con backend local seed
- Agregar tests de normalizacion de respuestas y render de estados clave
- Ajustar responsive para desktop y tablet; mobile como soporte funcional secundario
- Revisar copy operativo y contraste visual de estados

---

## 8. UI Notes

- Pantalla inicial unica, sin landing.
- Uso de tabs o secciones compactas; prioridad a escaneo rapido.
- Formularios con valores editables y acciones claras.
- Panel de respuesta visible sin tapar el formulario.
- Colores de estado diferenciados: aprobado, rechazo funcional, error tecnico, loading.
- Mostrar tambien el JSON crudo en bloque plegable para soporte y depuracion ligera.

---

## 9. Risks and Mitigations

- **Riesgo**: los IDs de `readerId` y `sessionId` no son estables en todos los entornos.  
  **Mitigacion**: presets editables y no hardcodear los IDs como constantes inmutables.

- **Riesgo**: inconsistencia de shapes de error entre endpoints.  
  **Mitigacion**: normalizador unico de respuestas antes de renderizar.

- **Riesgo**: el backend puede depender de integracion externa para tickets no locales.  
  **Mitigacion**: el MVP se concentra en tickets seed locales y muestra claramente rechazos o fallos tecnicos.

- **Riesgo**: asumir que toda respuesta `2xx` es exito.  
  **Mitigacion**: interpretar el payload funcional antes de pintar estado final.

---

## 10. Definition of Done

- Existe `frontend/` en el repo y levanta localmente.
- La app consume `localhost:8083` por configuracion de entorno.
- Se pueden ejecutar desde UI: consulta de ticket, ingreso, salida, reingreso y asignacion de puerta.
- La UI diferencia aprobacion, rechazo funcional y error tecnico.
- Hay presets basados en seed real del backend.
- Existe documentacion minima de arranque para correr backend y frontend juntos.

---

## 11. Success Criteria

- La app frontend permite demo local sin Swagger.
- Un operador puede probar el flujo basico usando `TICKET-ACTIVE-NORTH`, `TICKET-ENTERED` y `TICKET-EXITED`.
- Los errores funcionales se entienden desde UI sin inspeccionar logs.
- La implementacion inicial no requiere cambios de contrato en el backend.
