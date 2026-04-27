# Implementation Plan: Asignar Puerta de Ingreso (Clean Architecture - Java)

**Date**: 2026-04-11  
**Spec**: Asignar Puerta de Ingreso

---

## 1. Summary

Este proyecto implementa un sistema para asignar puertas de ingreso a categorías de tickets, permitiendo distribuir el flujo de asistentes en un evento y optimizar la operación de control de acceso.

El sistema permitirá configurar reglas dinámicas de acceso (puerta ↔ categoría), validar capacidad de flujo y propagar cambios en tiempo real a los dispositivos de ingreso. fileciteturn2file0L10-L34

La lógica se implementará en la capa **application (casos de uso)**, manteniendo Clean Architecture sin capa `service`.

---

## 2. Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 17  
**Primary Dependencies**: Spring Boot, Spring Web, Spring Data JPA, Lombok, JUnit 5, Mockito  
**Storage**: Base de datos relacional  
**Testing**: Unit, Integration y pruebas de propagación/configuración  
**Target Platform**: Backend para configuración operativa de accesos  
**Project Type**: Aplicación backend monolítica  
**Performance Goals**: Propagación de cambios en < 5 segundos  
**Constraints**: Consistencia de reglas, sincronización en tiempo real, evitar configuraciones inválidas  
**Scale/Scope**: Gestión de puertas, categorías, asignaciones y propagación a dispositivos

---

## 3. Architecture Approach

Controller → Use Case → Repository → Database

Principios:

- Clean Architecture
- Lógica en Use Cases
- Persistencia desacoplada
- Configuración dinámica

---

## 4. Project Structure

```text
ingreso/src/main/java/com/empresa/ingreso/

├── IngresoApplication.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── AssignGateCommand.java
│   │   │   ├── AssignGateResult.java
│   │   │   └── AssignGateUseCase.java
│   │   └── out/
│   │       ├── LoadGateAssignmentPort.java
│   │       ├── PublishGateAssignmentPort.java
│   │       └── SaveGateAssignmentPort.java
│   └── usecase/
│       └── DefaultAssignGateUseCase.java
├── domain/
│   └── model/
│       └── GateAssignment.java
├── infrastructure/
│   └── persistence/
│       ├── PersistenceAdapter.java
│       ├── entity/
│       │   └── GateAssignmentEntity.java
│       └── repository/
│           └── SpringDataGateAssignmentRepository.java
├── interfaces/
│   └── api/
│       ├── GateAssignmentController.java
│       └── dto/
│           ├── AssignGateRequest.java
│           └── AssignGateResponse.java
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
- Configurar DB
- Definir errores

---

## 6. Phase 2: Foundational

- Crear entidades Puerta, Categoria, Asignacion
- Crear repositorios
- Configurar persistencia
- Logging
- Manejo de errores

---

## 7. Phase 3: Caso de Uso Principal (P1)

### AsignarPuertaUseCase

- Crear asignación puerta-categoría
- Validar existencia
- Persistir configuración
- Actualizar reglas de acceso
- Propagar cambios a dispositivos

---

## 8. Phase 4: Distribución de accesos (P1)

- Asignar categorías a puertas
- Validar combinaciones
- Actualizar reglas de validación

---

## 9. Phase 5: Reasignación dinámica (P2)

- Permitir cambios en tiempo real
- Habilitar nuevas puertas
- Propagar cambios inmediatamente

---

## 10. Phase 6: Edge Cases

- Desasignar puerta con registros
- Categorías conflictivas

---

## 11. Phase 7: Propagación

- Enviar cambios a dispositivos
- Garantizar sincronización < 5s

---

## 12. Phase 8: Testing

- Unit tests
- Integration tests
- Configuración dinámica

---

## 13. Phase 9: Polish

- Documentación
- Optimización
- Métricas

---

## 14. Notes

- Sin capa service
- Lógica en application
- Interfaces al nivel de persistence

---

## 15. Success Criteria

- Propagación < 5s
- 0 errores de zona incorrecta
- Configuración en < 3 min
