# Implementation Plan: Procesamiento de Intentos de Ingreso (Clean Architecture - Java)

**Date**: 2026-04-11  

---

## 1. Summary

Este proyecto implementa un sistema de control de acceso para eventos mediante validación de tickets.  
El sistema permitirá autorizar o rechazar intentos de ingreso de asistentes, garantizando trazabilidad completa, control de concurrencia y cumplimiento de reglas de negocio.

La lógica de negocio se implementará en la capa de **application (casos de uso)**, manteniendo una arquitectura limpia sin uso de capa de servicios.

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
**Testing**: JUnit 5, Mockito, pruebas de integración y pruebas de concurrencia  
**Target Platform**: Backend transaccional para control de acceso a eventos  
**Project Type**: Aplicación backend monolítica con arquitectura limpia  
**Performance Goals**: Procesar cada intento de ingreso en menos de 2 segundos en condiciones normales  
**Constraints**: 0 doble ingreso por concurrencia, 100% de trazabilidad auditable, consistencia transaccional  
**Scale/Scope**: Validación de tickets, control por lector/zona, registro auditable de intentos, soporte de ingreso manual

---

## 3. Architecture Approach

Se adopta el enfoque de **Clean Architecture**, con separación clara de responsabilidades.

### Flujo principal

Controller → Use Case → Repository → Database

### Principios aplicados

- Separación de capas (domain, application, infrastructure)
- Dependencias dirigidas hacia el dominio
- Lógica de negocio centralizada en casos de uso
- Persistencia desacoplada mediante repositorios
- Control transaccional para garantizar consistencia
- Auditoría completa de todos los intentos

---

## 4. Project Structure

```text
src/main/java/com/empresa/ingreso/

├── domain/
│   ├── entities/
│   │   ├── Ticket.java
│   │   ├── IntentoIngreso.java
│   │   ├── RegistroIngreso.java
│   │   ├── Lector.java
│   │   └── SesionEvento.java
│   └── repositories/
│       ├── TicketRepository.java
│       ├── IntentoIngresoRepository.java
│       ├── RegistroIngresoRepository.java
│       └── LectorRepository.java

├── application/
│   ├── usecase/
│   │   ├── ProcesarIntentoIngresoUseCase.java
│   │   └── ProcesarIngresoManualUseCase.java
│   └── dto/
│       ├── ProcesarIntentoIngresoRequest.java
│       ├── ProcesarIntentoIngresoResponse.java
│       └── ErrorResponse.java

├── infrastructure/
│   ├── persistence/
│   │   ├── JpaTicketRepository.java
│   │   ├── JpaIntentoIngresoRepository.java
│   │   ├── JpaRegistroIngresoRepository.java
│   │   └── JpaLectorRepository.java
│   ├── interfaces/
│   │   └── api/
│   │       └── IngresoController.java
│   ├── config/
│   │   ├── AppConfig.java
│   │   └── TransactionConfig.java
│   └── concurrency/
│       └── TicketLockManager.java

├── shared/
│   ├── errors/
│   │   ├── ErrorCode.java
│   │   ├── BusinessException.java
│   │   └── TechnicalException.java
│   └── constants/
│       └── SystemConstants.java
```

---

## 5. Phase 1: Setup

**Objetivo**: Inicializar el proyecto y preparar el entorno de trabajo.

- Crear proyecto Java con Maven o Gradle
- Configurar Spring Boot
- Configurar conexión a base de datos
- Definir estructura de paquetes
- Configurar variables de entorno
- Definir diccionario de errores

---

## 6. Phase 2: Foundational

**Objetivo**: Construir la base técnica necesaria para soportar los casos de uso del sistema.

- Crear entidades: Ticket, IntentoIngreso, RegistroIngreso, Lector, SesionEvento
- Definir interfaces de repositorios
- Implementar repositorios con JPA
- Configurar transacciones
- Implementar logging
- Implementar manejo de errores
- Configurar sesión activa
- Implementar control de concurrencia

**Resultado esperado**: Sistema listo para soportar validaciones, persistencia y auditoría de intentos de ingreso.

---

## 7. Phase 3: Caso de Uso Principal

**Objetivo**: Procesar un intento de ingreso y decidir si se aprueba o se rechaza.

### Flujo

- Buscar ticket
- Validar existencia
- Validar estado
- Validar sesión
- Validar zona
- Validar duplicidad
- Registrar intento
- Crear RegistroIngreso
- Actualizar ticket
- Retornar respuesta

---

## 8. Phase 4: Rechazos

**Objetivo**: Manejar los casos de rechazo definidos por la especificación funcional.

- Ticket no encontrado
- Ticket duplicado
- Zona incorrecta
- Estado inválido
- Sesión inválida

Cada rechazo debe generar un intento fallido auditable con su código de error correspondiente.

---

## 9. Phase 5: Auditoría

**Objetivo**: Garantizar trazabilidad completa de todos los intentos de ingreso.

- Registrar todos los intentos
- Guardar contexto completo
- Almacenar fecha, lector, ticket, sesión, resultado y motivo de rechazo

---

## 10. Phase 6: Concurrencia

**Objetivo**: Evitar el doble ingreso de un mismo ticket.

- Control transaccional
- Evitar doble ingreso
- Garantizar atomicidad entre validación, registro y actualización

---

## 11. Phase 7: Ingreso Manual

**Objetivo**: Permitir el procesamiento manual cuando el lector QR falle.

- Implementar flujo manual
- Reutilizar validaciones
- Mantener trazabilidad equivalente al flujo principal

---

## 12. Phase 8: Edge Cases

**Objetivo**: Manejar condiciones límite y fallos operativos.

- Error DB
- Lector sin zona
- Fallos de persistencia
- Sesión no disponible
- Procesamiento simultáneo

---

## 13. Phase 9: Testing

**Objetivo**: Validar el comportamiento funcional y técnico del sistema.

- Unit tests
- Integration tests
- Concurrency tests

---

## 14. Phase 10: Polish

**Objetivo**: Mejorar calidad técnica y preparación para entrega.

- Documentación
- Optimización
- Métricas
- Revisión de errores y mensajes

---

## 15. Notes

- Lógica en use cases
- Sin capa service
- Arquitectura limpia
- Interfaces ubicadas al mismo nivel que persistence dentro de infrastructure

---

## 16. Success Criteria

- 100% intentos registrados
- Menor a 2 segundos de respuesta
- 0 duplicados por concurrencia
- Respuesta estructurada en todos los casos
