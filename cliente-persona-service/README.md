### Descripción general
Implementación completa del microservicio de gestión de clientes y personas con Spring Boot 4.0.4, incluyendo CRUD, mensajería asíncrona con RabbitMQ, manejo global de excepciones, y suite de tests unitarios.

---

### 🏗️ Arquitectura del proyecto

```
cliente-persona-service/
├── config/
│   └── RabbitMQConfig.java              # Configuración de exchange, queue y binding de RabbitMQ
├── controller/
│   └── ClienteController.java           # REST Controller con endpoints CRUD
├── dto/
│   ├── ClienteDTO.java                  # DTO de entrada con validaciones (Jakarta Validation)
│   └── ClienteResponseDTO.java          # DTO de respuesta (sin contraseña)
├── entity/
│   ├── Persona.java                     # Entidad base (Single Table Inheritance)
│   └── Cliente.java                     # Entidad hija con clienteId, contraseña y estado
├── exception/
│   ├── GlobalExceptionHandler.java      # @RestControllerAdvice para manejo centralizado de errores
│   └── ResourceNotFoundException.java   # Excepción personalizada → HTTP 404
├── mapper/
│   └── ClienteMapper.java              # @Component para conversión DTO ↔ Entity
├── repository/
│   └── ClienteRepository.java          # JPA Repository con queries personalizadas
├── service/
│   ├── ClienteService.java             # Interfaz del servicio
│   └── ClienteServiceImpl.java         # Implementación con transacciones y eventos RabbitMQ
└── test/
    └── controller/
        └── ClienteControllerTest.java   # 21 tests unitarios con MockMvc
```

---

### 🔌 Endpoints REST

| Método | Endpoint | Descripción | Response |
|--------|----------|-------------|----------|
| `POST` | `/clientes` | Crear nuevo cliente | `201 Created` |
| `GET` | `/clientes` | Listar todos los clientes | `200 OK` |
| `GET` | `/clientes/{clienteId}` | Obtener cliente por ID | `200 OK` / `404 Not Found` |
| `PUT` | `/clientes/{clienteId}` | Actualizar cliente | `200 OK` / `404 Not Found` |
| `DELETE` | `/clientes/{clienteId}` | Eliminar cliente (soft delete) | `204 No Content` / `404 Not Found` |

---

### 🗄️ Modelo de datos (JPA - Single Table Inheritance)

```
┌──────────────────────────────────┐
│          personas (table)        │
├──────────────────────────────────┤
│ persona_id (PK, auto-generated) │
│ tipo_persona (discriminator)     │
│ nombre (NOT NULL)                │
│ genero                           │
│ edad                             │
│ identificacion (UNIQUE)          │
│ direccion                        │
│ telefono                         │
│ cliente_id (UNIQUE, NOT NULL)    │  ← Solo para tipo CLIENTE
│ contrasena (NOT NULL)            │  ← Solo para tipo CLIENTE
│ estado (NOT NULL, default true)  │  ← Solo para tipo CLIENTE
└──────────────────────────────────┘
```

---

### 📨 Eventos RabbitMQ

| Evento | Routing Key | Cuándo se publica |
|--------|-------------|-------------------|
| Cliente creado | `cliente.event.created` | POST exitoso |
| Cliente actualizado | `cliente.event.updated` | PUT exitoso |
| Cliente eliminado | `cliente.event.deleted` | DELETE exitoso (soft delete) |

- **Exchange:** `cliente.exchange` (Topic)
- **Queue:** `cliente.events.queue` (durable)
- **Payload:** `{ clienteId, nombre, estado, action }`

---

### ✅ Validaciones (ClienteDTO)

| Campo | Regla | Mensaje |
|-------|-------|---------|
| `nombre` | `@NotBlank` | El nombre es obligatorio |
| `identificacion` | `@NotBlank` | La identificación es obligatoria |
| `contrasena` | `@NotBlank`, `@Size(min=4)` | La contraseña es obligatoria / debe tener al menos 4 caracteres |
| `edad` | `@Min(0)` | La edad debe ser un valor positivo |

---

### 🧪 Tests unitarios (`ClienteControllerTest`) — 21 tests

| Grupo | Test | Verifica |
|-------|------|----------|
| **Crear** | `crearClienteExitosamente` | POST → 201 con todos los campos |
| **Crear** | `crearClienteSinEstado` | Estado por defecto = true |
| **Crear** | `validacionFallaSinNombre` | 400 sin nombre |
| **Crear** | `validacionFallaSinIdentificacion` | 400 sin identificación |
| **Crear** | `validacionFallaSinContrasena` | 400 sin contraseña |
| **Crear** | `validacionFallaContrasenaCorta` | 400 contraseña < 4 chars |
| **Crear** | `validacionFallaEdadNegativa` | 400 edad negativa |
| **Crear** | `validacionFallaBodyVacio` | 400 body `{}` |
| **Crear** | `crearClienteConIdentificacionDuplicada` | 400 IllegalArgumentException |
| **Listar** | `listarTodosLosClientes` | 200 con 2 clientes |
| **Listar** | `listarClientesVacio` | 200 con lista vacía |
| **Listar** | `listarUnSoloCliente` | 200 con 1 cliente |
| **Obtener** | `obtenerClientePorId` | 200 con todos los campos |
| **Obtener** | `clienteNoEncontrado` | 404 con mensaje de error |
| **Actualizar** | `actualizarClienteExitosamente` | 200 con datos actualizados |
| **Actualizar** | `actualizarClienteNoEncontrado` | 404 cliente inexistente |
| **Actualizar** | `actualizarClienteValidacionFalla` | 400 sin campos obligatorios |
| **Eliminar** | `eliminarClienteExitosamente` | 204 soft delete |
| **Eliminar** | `eliminarClienteNoEncontrado` | 404 cliente inexistente |
| **Otros** | `errorInternoAlCrear` | 500 RuntimeException |
| **Otros** | `errorInternoAlListar` | 500 RuntimeException |

---

### 📦 Dependencias principales

| Dependencia | Propósito |
|-------------|-----------|
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-data-jpa` | Persistencia con Hibernate |
| `spring-boot-starter-validation` | Validación Jakarta |
| `spring-boot-starter-amqp` | RabbitMQ |
| `postgresql` | Driver PostgreSQL |
| `h2` (test) | Base de datos en memoria para tests |
| `spring-boot-starter-test` | Testing framework |
| `spring-boot-starter-webmvc-test` | MockMvc para SB 4.x |
| `testcontainers` | PostgreSQL y RabbitMQ en contenedores |
| `lombok` | Reducción de boilerplate |

---

### ⚙️ Configuración

- **Puerto:** `8081`
- **Base de datos:** PostgreSQL `localhost:5432/cliente_persona_db`
- **RabbitMQ:** `localhost:5672` (guest/guest)
- **DDL:** `hibernate.ddl-auto=update`