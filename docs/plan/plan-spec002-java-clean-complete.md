# Implementation Plan: Procesamiento de Intentos de Ingreso (Clean Architecture - Java)

**Date**: 2026-04-11

---

## 1. Summary

Este proyecto implementa un sistema de control de acceso para eventos mediante validacion de tickets.
El sistema permitira autorizar o rechazar intentos de ingreso de asistentes, garantizando trazabilidad completa, control de concurrencia y cumplimiento de reglas de negocio.

La logica de negocio se implementara en la capa de **application (casos de uso)**, manteniendo una arquitectura limpia sin uso de capa de servicios.

---

## 2. Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot, Spring Web, Spring Data JPA, Lombok, JUnit 5, Mockito
**Storage**: Base de datos relacional
**Testing**: JUnit 5, Mockito, pruebas de integracion y pruebas de concurrencia
**Target Platform**: Backend transaccional para control de acceso a eventos
**Project Type**: Aplicacion backend monolitica con arquitectura limpia
**Performance Goals**: Procesar cada intento de ingreso en menos de 2 segundos en condiciones normales
**Constraints**: 0 doble ingreso por concurrencia, 100% de trazabilidad auditable, consistencia transaccional, orden fijo de validaciones y error deterministico
**Scale/Scope**: Validacion de tickets, control por lector/zona, registro auditable de intentos, soporte de ingreso manual y actualizacion opcional de ocupacion del evento o zona

---

## 3. Architecture Approach

Se adopta el enfoque de **Clean Architecture**, con separacion clara de responsabilidades.

### Flujo principal

Controller -> Use Case -> Repository -> Database

### Principios aplicados

- Separacion de capas (`domain`, `application`, `infrastructure`)
- Dependencias dirigidas hacia el dominio
- Logica de negocio centralizada en casos de uso
- Persistencia desacoplada mediante repositorios
- Control transaccional para garantizar consistencia
- Auditoria completa de todos los intentos
- Precedencia explicita de validaciones para evitar respuestas ambiguas
- Concurrencia resuelta con garantias de base de datos, no solo con locks en memoria

---

## 4. Project Structure

```text
src/main/java/com/empresa/ingreso/

|-- IngresoApplication.java
|
|-- application/
|   |-- port/
|   |   |-- in/
|   |   |   |-- ProcessEntryAttemptCommand.java
|   |   |   |-- ProcessEntryAttemptResult.java
|   |   |   `-- ProcessEntryAttemptUseCase.java
|   |   `-- out/
|   |       |-- LoadEventSessionPort.java
|   |       |-- LoadReaderDevicePort.java
|   |       |-- LoadTicketPort.java
|   |       |-- SaveAccessAttemptPort.java
|   |       |-- SaveEntryRecordPort.java
|   |       `-- SaveTicketPort.java
|   `-- usecase/
|       `-- DefaultProcessEntryAttemptUseCase.java
|
|-- domain/
|   `-- model/
|       |-- AccessAttempt.java
|       |-- AccessChannel.java
|       |-- AccessType.java
|       |-- AttemptResult.java
|       |-- EntryRecord.java
|       |-- EventSession.java
|       |-- ReaderDevice.java
|       |-- Ticket.java
|       `-- TicketStatus.java
|
|-- infrastructure/
|   `-- persistence/
|       |-- PersistenceAdapter.java
|       |-- dto/
|       |-- entity/
|       |   |-- AccessAttemptEntity.java
|       |   |-- EntryRecordEntity.java
|       |   |-- EventSessionEntity.java
|       |   |-- ReaderDeviceEntity.java
|       |   `-- TicketEntity.java
|       `-- repository/
|           |-- SpringDataAccessAttemptRepository.java
|           |-- SpringDataEntryRecordRepository.java
|           |-- SpringDataEventSessionRepository.java
|           |-- SpringDataReaderDeviceRepository.java
|           `-- SpringDataTicketRepository.java
|
|-- interfaces/
|   `-- api/
|       |-- EntryController.java
|       |-- GlobalExceptionHandler.java
|       `-- dto/
|           |-- ProcessEntryAttemptRequest.java
|           `-- ProcessEntryAttemptResponse.java
|
`-- shared/
    `-- errors/
        |-- BusinessException.java
        |-- ErrorCode.java
        `-- TechnicalException.java
```

```text
src/test/java/com/empresa/ingreso/

|-- IngresoApplicationTests.java
`-- application/
    `-- usecase/
        `-- DefaultProcessEntryAttemptUseCaseTest.java
```

---

## 5. Phase 1: Setup

**Objetivo**: Inicializar el proyecto y preparar el entorno de trabajo.

- Crear proyecto Java con Maven o Gradle
- Configurar Spring Boot
- Configurar conexion a base de datos
- Definir estructura de paquetes
- Configurar variables de entorno
- Definir diccionario de errores
- Acordar la estrategia de concurrencia en base de datos

---

## 6. Phase 2: Foundational

**Objetivo**: Construir la base tecnica necesaria para soportar los casos de uso del sistema.

- Crear entidades: `Ticket`, `IntentoIngreso`, `RegistroIngreso`, `Lector`, `SesionEvento`
- Definir puertos de entrada y salida en `application.port`
- Implementar adaptador de persistencia y repositorios JPA en `infrastructure.persistence`
- Configurar transacciones
- Implementar logging
- Implementar manejo de errores
- Configurar sesion activa
- Implementar control de concurrencia
- Modelar `IntentoIngreso` con `ticketId` nullable y `codigoTicketIngresado`
- Modelar ocupacion del evento o zona como capacidad opcional dentro del mismo agregado transaccional o modulo separado

**Resultado esperado**: Sistema listo para soportar validaciones, persistencia y auditoria de intentos de ingreso.

---

## 7. Phase 3: Caso de Uso Principal

**Objetivo**: Procesar un intento de ingreso y decidir si se aprueba o se rechaza.

### Flujo

- Validar lector y puerta
- Buscar ticket
- Validar existencia
- Validar estado
- Validar sesion
- Validar zona
- Validar duplicidad
- Registrar intento
- Crear `RegistroIngreso`
- Actualizar ticket
- Actualizar ocupacion si aplica
- Retornar respuesta

### Contrato de respuesta

- `status`: `APROBADO` o `RECHAZADO`
- `message`: texto operativo
- `errorCode`: nulo en exito, obligatorio en rechazo
- `attemptId`: identificador auditable del intento

---

## 8. Phase 4: Rechazos

**Objetivo**: Manejar los casos de rechazo definidos por la especificacion funcional.

- Ticket no encontrado
- Ticket duplicado
- Zona incorrecta
- Estado invalido
- Sesion invalida
- Lector no configurado

Cada rechazo debe generar un intento fallido auditable con su codigo de error correspondiente.

---

## 9. Phase 5: Auditoria

**Objetivo**: Garantizar trazabilidad completa de todos los intentos de ingreso.

- Registrar todos los intentos
- Guardar contexto completo
- Almacenar fecha, lector, puerta, `codigoTicketIngresado`, ticket nullable, sesion, canal, resultado y motivo de rechazo

---

## 10. Phase 6: Concurrencia

**Objetivo**: Evitar el doble ingreso de un mismo ticket.

- Control transaccional
- Evitar doble ingreso
- Garantizar atomicidad entre validacion, registro y actualizacion
- Definir estrategia persistente: bloqueo pesimista, versionado optimista o restriccion unica segun el modelo real
- Evitar depender solo de locks en memoria para soportar multiples instancias

---

## 11. Phase 7: Ingreso Manual

**Objetivo**: Permitir el procesamiento manual cuando el lector QR falle.

- Implementar flujo manual
- Reutilizar validaciones
- Mantener trazabilidad equivalente al flujo principal
- Registrar `canalEntrada = MANUAL`

---

## 12. Phase 8: Edge Cases

**Objetivo**: Manejar condiciones limite y fallos operativos.

- Error DB
- Lector sin zona
- Fallos de persistencia
- Sesion no disponible
- Procesamiento simultaneo
- Ticket inexistente con persistencia de intento fallido sin referencia de ticket

---

## 13. Phase 9: Testing

**Objetivo**: Validar el comportamiento funcional y tecnico del sistema.

- Unit tests
- Integration tests
- Concurrency tests
- Tests de precedencia de errores
- Tests del contrato de respuesta estructurada

---

## 14. Phase 10: Polish

**Objetivo**: Mejorar calidad tecnica y preparacion para entrega.

- Documentacion
- Optimizacion
- Metricas
- Revision de errores y mensajes

---

## 15. Notes

- Logica en use cases
- Sin capa service
- Arquitectura limpia
- Los contratos del caso de uso viven en `application.port.in` y `application.port.out`
- Los DTO HTTP viven en `interfaces.api.dto`
- `interfaces` e `infrastructure` son paquetes hermanos bajo `com.empresa.ingreso`
- El orden de validaciones debe mantenerse estable: lector, existencia, estado, sesion, zona, duplicidad
- Si el modulo de ocupacion no existe aun, la actualizacion de capacidad debe desacoplarse o marcarse fuera de alcance tecnico inmediato

---

## 16. Success Criteria

- 100% intentos registrados
- Menor a 2 segundos de respuesta
- 0 duplicados por concurrencia
- Respuesta estructurada en todos los casos
- Error deterministico segun orden de validaciones
