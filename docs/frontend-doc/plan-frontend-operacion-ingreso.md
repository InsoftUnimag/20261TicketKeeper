# Implementation Plan: Frontend de Operacion de Ingreso

**Date**: 2026-05-09
**Spec**: `docs/frontend-doc/spec-frontend-operacion-ingreso.md`

---

## 1. Summary

Se implementara un frontend operativo dentro del repo para consumir la API de ingreso ya disponible. El objetivo inicial no es administrar todo el sistema, sino ofrecer una consola de pruebas y operacion con formularios claros para consulta de ticket, ingreso, salida, reingreso y asignacion de puerta.

El frontend debe servir para reemplazar las pruebas manuales en Swagger en los flujos mas importantes, usando datos semilla y mostrando de forma clara respuestas exitosas y errores funcionales.

---

## 2. Technical Context

**Language/Version**: TypeScript  
**Primary Dependencies**: React, Vite, cliente HTTP liviano (`fetch` nativo o `axios`)  
**Storage**: N/A  
**Testing**: pruebas manuales iniciales; opcionalmente Vitest mas adelante  
**Target Platform**: navegador web en entorno local  
**Project Type**: aplicacion web separada dentro del repo  
**Performance Goals**: interaccion fluida y respuestas visibles en menos de 1 segundo despues del retorno del backend  
**Constraints**: backend corre en `localhost`, CORS ya configurado para `3000` y `5173`, debe ser utilitario y no marketing  
**Scale/Scope**: 1 app web, 5 vistas funcionales o 1 dashboard con 5 modulos operativos

---

## 3. Architecture Approach

Frontend React/Vite con estructura simple:

- `pages` o una vista principal con secciones operativas
- `components` para formularios y paneles de respuesta
- `services` para encapsular llamadas HTTP
- `types` para tipar requests y responses de la API

El frontend no debe duplicar reglas de negocio del backend. Solo valida formato basico y presenta respuestas.

---

## 4. Project Structure

```text
ticker1/
├── docs/
│   └── frontend-doc/
│       ├── plan-frontend-operacion-ingreso.md
│       └── spec-frontend-operacion-ingreso.md
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── types/
│   │   └── main.tsx
│   └── package.json
└── ingreso/
    └── ...
```

**Structure Decision**: usar `frontend/` sin espacios para evitar problemas con scripts, tooling y rutas.

---

## 5. Phase 1: Setup

- Crear app en `ticker1/frontend`
- Configurar Vite con React y TypeScript
- Definir URL base del backend por variable de entorno
- Configurar estructura base de carpetas

---

## 6. Phase 2: Foundational

- Crear cliente HTTP para la API de ingreso
- Tipar DTOs minimos necesarios
- Crear layout principal de operacion
- Crear componente reutilizable para mostrar respuestas y errores
- Definir datos semilla sugeridos visibles en la UI para pruebas rapidas

---

## 7. Phase 3: User Story 1 - Consulta de ticket (P1)

**Goal**: consultar estado de un ticket desde una interfaz simple.

**Independent Test**: buscar `TICKET-ACTIVE-NORTH` y visualizar la respuesta de `GET /api/v1/tickets/{ticketCode}/status`.

- Implementar formulario de consulta por `ticketCode`
- Consumir endpoint de estado
- Mostrar `ticketStatus`, `sessionId`, `gateId`, `entryAt`, `message` y `errorCode`

---

## 8. Phase 4: User Story 2 - Ingreso inicial (P1)

**Goal**: registrar intentos de ingreso y visualizar aprobacion o rechazo.

**Independent Test**: enviar un ticket semilla valido y ver el resultado del backend.

- Implementar formulario de intento de ingreso
- Consumir `POST /api/v1/entry-attempts`
- Mostrar payload de respuesta en un panel consistente

---

## 9. Phase 5: User Story 3 - Salida y reingreso (P2)

**Goal**: operar el flujo completo de acceso.

**Independent Test**: registrar salida de un ticket compatible y luego intentar reingreso.

- Implementar formulario de salida
- Implementar formulario de reingreso
- Reutilizar componente base de operacion

---

## 10. Phase 6: User Story 4 - Asignacion de puerta (P3)

**Goal**: permitir configuracion operativa desde UI.

**Independent Test**: crear una asignacion valida usando una sesion existente del seed.

- Implementar formulario de asignacion de puerta
- Mostrar conflictos y errores de sesion inexistente

---

## 11. Phase 7: Polish

- Ajustar textos y validaciones del formulario
- Mostrar ejemplos de datos semilla para prueba rapida
- Agregar `README` del frontend con instrucciones de arranque
- Verificar estilos responsive de escritorio y mobile

---

## 12. Notes

- El frontend debe empezar como herramienta operativa, no como portal corporativo.
- La primera version puede resolverse en una sola pantalla con modulos plegables o tabs.
- Los ejemplos visibles al usuario deben usar datos semilla reales, no valores ficticios como `42`.
- El backend ya expone CORS para puertos tipicos de desarrollo local.

---

## 13. Success Criteria

- La app frontend levanta localmente y se conecta al backend sin tocar Swagger.
- Un operador puede ejecutar al menos consulta de ticket e ingreso inicial en la primera iteracion.
- Los errores funcionales de la API se entienden desde UI sin inspeccionar logs.
