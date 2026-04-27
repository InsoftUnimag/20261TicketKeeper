# Implementation Plan: Consultar Registro de Ingreso (Clean Architecture - Java)

**Date**: 2026-04-11  
**Spec**: Consultar Registro de Ingreso

---

## 1. Summary

Este proyecto implementa un sistema de consulta de registros de ingreso para eventos, permitiendo a módulos externos (como liquidación) obtener el estado final de asistencia de los tickets.

El sistema permitirá consultar tanto registros individuales por ticket como listados completos por evento, garantizando consistencia, trazabilidad y respuestas en tiempo real. fileciteturn4file0L10-L30

La lógica se implementa en la capa **application (use cases)** manteniendo Clean Architecture sin capa `service`.

---

## 2. Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot, Spring Web, Spring Data JPA, Lombok, JUnit 5, Mockito  
**Storage**: Base de datos relacional  
**Testing**: Unit, Integration  
**Target Platform**: Backend para consulta operativa y financiera  
**Project Type**: Backend monolítico con Clean Architecture  
**Performance Goals**: < 2 segundos por consulta  
**Constraints**: Consistencia de datos, no modificación de estado, trazabilidad de consultas  
**Scale/Scope**: Consulta por ticket, consulta por evento, logs de auditoría

---

## 3. Architecture Approach

Controller → Use Case → Repository → Database

Principios:
- Lógica en Use Cases
- Sin capa service
- Persistencia desacoplada
- Operación de solo lectura

---

## 4. Project Structure

```text
ingreso/src/main/java/com/empresa/ingreso/

├── IngresoApplication.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── GetEntryRecordByTicketResult.java
│   │   │   ├── GetEntryRecordByTicketUseCase.java
│   │   │   └── GetEntryRecordsByEventUseCase.java
│   │   └── out/
│   │       ├── LoadEntryRecordPort.java
│   │       ├── LoadEventSessionPort.java
│   │       └── LoadTicketPort.java
│   └── usecase/
│       ├── DefaultGetEntryRecordByTicketUseCase.java
│       └── DefaultGetEntryRecordsByEventUseCase.java
├── domain/
│   └── model/
│       ├── EntryRecord.java
│       ├── EventSession.java
│       └── Ticket.java
├── infrastructure/
│   └── persistence/
│       ├── PersistenceAdapter.java
│       ├── entity/
│       │   ├── EntryRecordEntity.java
│       │   ├── EventSessionEntity.java
│       │   └── TicketEntity.java
│       └── repository/
│           ├── SpringDataEntryRecordRepository.java
│           ├── SpringDataEventSessionRepository.java
│           └── SpringDataTicketRepository.java
├── interfaces/
│   └── api/
│       ├── EntryRecordQueryController.java
│       └── dto/
│           └── EntryRecordResponse.java
└── shared/
    └── errors/
        ├── BusinessException.java
        ├── ErrorCode.java
        └── TechnicalException.java
```

---

## 5. Phase 1: Setup

- Crear proyecto Java
- Configurar Spring Boot
- Configurar base de datos
- Definir errores

---

## 6. Phase 2: Foundational

- Crear entidades RegistroIngreso, Ticket, Evento
- Crear repositorios
- Configurar persistencia
- Logging
- Manejo de errores

---

## 7. Phase 3: Caso de Uso Principal (P1)

### ConsultarRegistroPorTicketUseCase

- Buscar ticket
- Validar existencia
- Buscar registro de ingreso
- Determinar estado final
- Construir respuesta
- Registrar log
- Retornar resultado

---

## 8. Phase 4: Consulta por evento (P2)

### ConsultarRegistrosPorEventoUseCase

- Validar existencia del evento
- Obtener lista de registros
- Construir respuesta
- Retornar lista

---

## 9. Phase 5: Edge Cases

- Ticket no encontrado
- Evento no encontrado
- Sin registros
- Inconsistencias de datos

---

## 10. Phase 6: Consistencia

- Priorizar registro de ingreso como fuente de verdad
- Registrar inconsistencias en logs

---

## 11. Phase 7: Testing

- Unit tests
- Integration tests
- Validación de consistencia

---

## 12. Phase 8: Polish

- Documentación
- Optimización
- Logs

---

## 13. Notes

- Lógica en use cases
- Sin service
- Interfaces junto a persistence
- Operación de solo lectura

---

## 14. Success Criteria

- 100% respuestas correctas
- < 2s respuesta
- 0 inconsistencias
