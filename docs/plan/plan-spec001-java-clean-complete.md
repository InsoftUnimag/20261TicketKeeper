# Implementation Plan: Informar Estado del Ticket (Clean Architecture - Java)

**Date**: 2026-04-11  
**Spec**: Informar estado del tickect

---

## 1. Summary

Este proyecto implementa un sistema de consulta del estado de tickets para eventos, permitiendo al personal de control de acceso informar al asistente si un ticket es válido, ya fue utilizado, está cancelado, bloqueado o presenta alguna condición inválida, sin procesar un ingreso ni modificar el estado del ticket.

La lógica de negocio se implementará en la capa de **application (casos de uso)**, manteniendo una arquitectura limpia sin uso de capa de servicios. El sistema deberá consultar tickets por código único, devolver un resultado estructurado, reflejar el estado actual con consistencia fuerte y registrar la consulta para auditoría cuando la configuración lo permita.

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
**Testing**: JUnit 5, Mockito, pruebas de integración y pruebas de consistencia de lectura  
**Target Platform**: Backend transaccional para consulta operativa de tickets en eventos  
**Project Type**: Aplicación backend monolítica con arquitectura limpia  
**Performance Goals**: Procesar cada consulta en menos de 1 segundo en condiciones normales  
**Constraints**: 0 modificaciones del estado del ticket durante la consulta, 100% de respuestas estructuradas, consistencia fuerte para reflejar uso reciente del ticket  
**Scale/Scope**: Consulta por código único, lectura de estado actual, información de sesión/evento, auditoría configurable de consultas y manejo de errores técnicos u operativos

---

## 3. Architecture Approach

Se adopta el enfoque de **Clean Architecture**, con separación clara de responsabilidades.

### Flujo principal

Controller → Use Case → Repository → Database

### Principios aplicados

- Separación de capas (`domain`, `application`, `infrastructure`)
- Dependencias dirigidas hacia el dominio
- Lógica de negocio centralizada en casos de uso
- Persistencia desacoplada mediante repositorios
- Consulta inmutable: la operación no modifica el estado del ticket
- Auditoría configurable de las consultas
- Consistencia fuerte para reflejar el estado más reciente del ticket

---

## 4. Project Structure

```text
src/main/java/com/empresa/ingreso/

├── domain/
│   ├── entities/
│   │   ├── Ticket.java
│   │   ├── ConsultaTicket.java
│   │   ├── RegistroIngreso.java
│   │   └── SesionEvento.java
│   └── repositories/
│       ├── TicketRepository.java
│       ├── ConsultaTicketRepository.java
│       ├── RegistroIngresoRepository.java
│       └── SesionEventoRepository.java

├── application/
│   ├── usecase/
│   │   └── InformarEstadoTicketUseCase.java
│   └── dto/
│       ├── InformarEstadoTicketRequest.java
│       ├── InformarEstadoTicketResponse.java
│       └── ErrorResponse.java

├── infrastructure/
│   ├── persistence/
│   │   ├── JpaTicketRepository.java
│   │   ├── JpaConsultaTicketRepository.java
│   │   ├── JpaRegistroIngresoRepository.java
│   │   └── JpaSesionEventoRepository.java
│   ├── interfaces/
│   │   └── api/
│   │       └── ConsultaTicketController.java
│   ├── config/
│   │   ├── AppConfig.java
│   │   └── QueryConfig.java
│   └── consistency/
│       └── TicketReadConsistencyPolicy.java

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
- Definir diccionario de errores para consultas
- Configurar flag de auditoría opcional de consultas

### Dependencias sugeridas

- Java 17+
- Spring Boot
- Spring Web
- Spring Data JPA
- Lombok
- JUnit 5
- Mockito
- Base de datos relacional

---

## 6. Phase 2: Foundational

**Objetivo**: Construir la base técnica necesaria para soportar el caso de uso de consulta.

- Crear entidades:
  - `Ticket`
  - `ConsultaTicket`
  - `RegistroIngreso`
  - `SesionEvento`
- Definir interfaces de repositorios
- Implementar repositorios con JPA
- Configurar consultas de solo lectura
- Implementar logging
- Implementar manejo de errores
- Configurar acceso a sesión/evento asociado al ticket
- Configurar política de consistencia fuerte para lectura del estado más reciente
- Configurar auditoría opcional de consultas

**Resultado esperado**: Sistema listo para consultar el estado actual de tickets sin alterar su información.

---

## 7. Phase 3: Caso de Uso Principal

### InformarEstadoTicketUseCase

**Objetivo**: Consultar el estado de un ticket existente y devolver una respuesta clara sin modificar el ticket.

### Flujo funcional

- Buscar ticket por código único
- Validar que el ticket exista
- Obtener estado actual del ticket
- Verificar si existe registro previo de check-in
- Obtener información de sesión/evento asociado
- Determinar mensaje descriptivo según estado y uso
- Construir respuesta estructurada
- Registrar la consulta para auditoría si la configuración lo habilita
- Retornar respuesta sin modificar el estado del ticket

### Respuestas esperadas del caso de uso

- `Ticket válido – no utilizado`
- `Ticket ya utilizado`
- `Ticket cancelado – ingreso no permitido`
- `Ticket bloqueado`
- `Ticket con datos inconsistentes`
- `Ticket no encontrado`
- `Error técnico`

---

## 8. Phase 4: User Story 1 - Consultar estado de un ticket válido (P1)

**Objetivo**: Permitir al encargado consultar un ticket existente y conocer su estado actual sin procesar ingreso.

### Escenarios cubiertos

- Ticket activo y no usado
- Ticket ya utilizado con fecha/hora de ingreso

### Tareas

- Implementar búsqueda de ticket por código
- Implementar lectura del estado actual
- Consultar si existe `RegistroIngreso`
- Mostrar fecha/hora del ingreso cuando aplique
- Devolver mensaje claro y comprensible
- Garantizar que la operación no modifique el ticket

---

## 9. Phase 5: User Story 2 - Informar ticket con estado inválido (P2)

**Objetivo**: Permitir informar correctamente cuando el ticket se encuentra cancelado, bloqueado u otro estado inválido.

### Escenarios cubiertos

- Ticket cancelado
- Ticket bloqueado

### Tareas

- Implementar mapeo de estados inválidos a mensajes descriptivos
- Devolver respuesta estructurada con estado y mensaje
- Mantener la consulta como operación de solo lectura
- Registrar auditoría de consulta cuando esté habilitada

---

## 10. Phase 6: Edge Cases

**Objetivo**: Manejar los casos límite definidos por la especificación.

- Ticket existe pero tiene datos incompletos
- Base de datos no disponible
- Ticket usado hace segundos y el sistema debe reflejar el estado actualizado

### Tareas

- Detectar datos inconsistentes del ticket
- Devolver mensaje `Ticket con datos inconsistentes` y registrar el error
- Manejar indisponibilidad de base de datos como error técnico
- Implementar lectura consistente para reflejar cambios recientes
- Evitar respuestas desactualizadas frente a uso reciente del ticket

---

## 11. Phase 7: Auditoría

**Objetivo**: Garantizar trazabilidad de las consultas cuando la configuración lo permita.

- Registrar la consulta de ticket para auditoría
- Guardar:
  - fecha y hora
  - ticket consultado o referencia nula si no existe
  - actor
  - resultado de la consulta
- Respetar configuración para activar o desactivar auditoría
- Mantener logs técnicos de soporte

---

## 12. Phase 8: Inmutabilidad de la Consulta

**Objetivo**: Garantizar que la consulta no altere la información del ticket.

- Marcar el caso de uso como operación de lectura
- Prohibir actualizaciones del ticket durante la consulta
- Validar mediante pruebas que no se produzcan cambios de estado
- Asegurar que la consulta no cree registros de ingreso ni cambie indicadores de uso

---

## 13. Phase 9: Testing

**Objetivo**: Validar el comportamiento funcional y técnico del sistema.

### Unit Tests

- Validación de existencia del ticket
- Resolución del estado actual del ticket
- Validación de ticket usado previamente
- Construcción de mensajes descriptivos
- Manejo de ticket cancelado
- Manejo de ticket bloqueado
- Manejo de ticket con datos inconsistentes
- Confirmación de no modificación del ticket

### Integration Tests

- Consulta de ticket válido no utilizado
- Consulta de ticket ya utilizado
- Consulta de ticket cancelado
- Consulta de ticket bloqueado
- Consulta de ticket inexistente
- Error técnico por indisponibilidad de base de datos
- Auditoría de consulta habilitada
- Auditoría de consulta deshabilitada

### Consistency Tests

- Consulta inmediatamente después de registrar uso del ticket
- Verificación de lectura actualizada del estado
- Verificación de ausencia de cambios en el ticket tras la consulta

---

## 14. Phase 10: Polish

**Objetivo**: Mejorar calidad técnica y preparación para entrega.

- Documentación técnica del flujo de consulta
- Documentación de mensajes y códigos de error
- Optimización de performance
- Revisión de logs y mensajes mostrados al personal
- Validación de tiempo de respuesta menor a 1 segundo
- Revisión de configuración de auditoría
- Revisión de nombres y paquetes

---

## 15. Notes

- Toda la lógica de negocio se concentra en `application/usecase`
- No se utiliza capa `service`
- Se mantiene arquitectura limpia
- `interfaces` se ubica al mismo nivel que `persistence` dentro de `infrastructure`
- La operación de consulta es estrictamente de solo lectura
- La auditoría de consultas es configurable
- Se debe garantizar:
  - 100% de respuestas claras y comprensibles
  - 0 cambios en el estado del ticket durante la consulta
  - detección correcta de tickets inexistentes
  - consistencia fuerte frente a uso reciente del ticket

---

## 16. Success Criteria

- 100% de las consultas devuelven un estado claro y comprensible
- Tiempo de respuesta menor a 1 segundo en condiciones normales
- 0% de consultas generan cambios en el estado del ticket
- 100% de tickets inexistentes son detectados correctamente
- El personal puede informar el estado del ticket sin necesidad de procesar ingreso
