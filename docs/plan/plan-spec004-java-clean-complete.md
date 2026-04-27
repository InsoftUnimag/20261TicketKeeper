# Implementation Plan: Registrar Re-ingreso (Clean Architecture - Java)

**Date**: 2026-04-11  
**Spec**: Registrar re-ingreso

---

## 1. Summary

Este proyecto implementa el control de re-ingreso de asistentes a un evento, permitiendo validar políticas configuradas como permiso de reingreso, límite de intentos y registro de salidas.

El sistema permitirá autorizar o rechazar re-ingresos, registrar cada intento y mantener trazabilidad completa del flujo de entrada/salida.

La lógica se implementa en la capa **application (use cases)** manteniendo Clean Architecture sin capa service.

---

## 2. Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot, Spring Web, Spring Data JPA, Lombok, JUnit 5, Mockito  
**Storage**: Base de datos relacional  
**Testing**: Unit, Integration, Concurrency  
**Target Platform**: Backend transaccional de control de accesos  
**Project Type**: Backend monolítico con Clean Architecture  
**Performance Goals**: < 2 segundos por validación  
**Constraints**: Control de concurrencia, consistencia, auditoría completa  
**Scale/Scope**: Re-ingreso, salida, validaciones, control de límites  

---

## 3. Architecture Approach

Controller → Use Case → Repository → Database

Principios:
- Lógica en Use Cases
- Sin capa service
- Persistencia desacoplada
- Control transaccional

---

## 4. Project Structure

```text
ingreso/src/main/java/com/empresa/ingreso/

├── IngresoApplication.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── ReEntryCommand.java
│   │   │   ├── ReEntryResult.java
│   │   │   ├── ReEntryUseCase.java
│   │   │   ├── RegisterExitCommand.java
│   │   │   ├── RegisterExitResult.java
│   │   │   └── RegisterExitUseCase.java
│   │   └── out/
│   │       ├── LoadEntryRecordPort.java
│   │       ├── LoadTicketPort.java
│   │       ├── SaveAccessAttemptPort.java
│   │       ├── SaveEntryRecordPort.java
│   │       └── SaveTicketPort.java
│   └── usecase/
│       ├── DefaultReEntryUseCase.java
│       └── DefaultRegisterExitUseCase.java
├── domain/
│   └── model/
│       ├── AccessAttempt.java
│       ├── EntryRecord.java
│       ├── Ticket.java
│       └── TicketStatus.java
├── infrastructure/
│   └── persistence/
│       ├── PersistenceAdapter.java
│       ├── entity/
│       │   ├── AccessAttemptEntity.java
│       │   ├── EntryRecordEntity.java
│       │   └── TicketEntity.java
│       └── repository/
│           ├── SpringDataAccessAttemptRepository.java
│           ├── SpringDataEntryRecordRepository.java
│           └── SpringDataTicketRepository.java
├── interfaces/
│   └── api/
│       ├── ReEntryController.java
│       └── dto/
│           ├── ReEntryRequest.java
│           ├── ReEntryResponse.java
│           ├── RegisterExitRequest.java
│           └── RegisterExitResponse.java
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

- Crear entidades Ticket, IntentoIngreso, Evento
- Crear repositorios
- Configurar persistencia
- Logging
- Manejo de errores
- Concurrencia

---

## 7. Phase 3: Caso de Uso Principal

### RegistrarReingresoUseCase

- Validar ticket
- Validar ingreso previo
- Validar política de reingreso
- Validar límite
- Registrar intento
- Registrar reingreso
- Retornar respuesta

---

## 8. Phase 4: Rechazos

- Reingreso no permitido
- Límite excedido
- Estado inválido

---

## 9. Phase 5: Control de límites

- Contar reingresos
- Validar máximo permitido

---

## 10. Phase 6: Registro de salida

- Registrar salida
- Validar estado previo
- Evitar doble salida

---

## 11. Phase 7: Concurrencia

- Evitar doble reingreso
- Transacciones

---

## 12. Phase 8: Edge Cases

- Sin ingreso previo
- Ticket cancelado
- Concurrencia simultánea

---

## 13. Phase 9: Testing

- Unit tests
- Integration tests
- Concurrency tests

---

## 14. Phase 10: Polish

- Documentación
- Optimización
- Métricas

---

## 15. Notes

- Lógica en use cases
- Sin service
- Interfaces junto a persistence

---

## 16. Success Criteria

- 100% registros correctos
- 0 accesos indebidos
- < 2s respuesta
