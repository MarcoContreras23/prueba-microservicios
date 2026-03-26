# Cuenta Movimientos Service

Microservicio de gestión de **Cuentas**, **Movimientos** y **Reportes** desarrollado con Spring Boot 4.0.4 y Java 17. Forma parte de una arquitectura de microservicios que se comunica de forma asíncrona con el servicio `cliente-persona-service` mediante **RabbitMQ**.

---

## Stack Tecnológico

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.4 |
| Spring Data JPA | (managed by Spring Boot) |
| PostgreSQL | Runtime |
| RabbitMQ (AMQP) | Spring Boot Starter |
| Lombok | Latest |
| Jackson | 3.1.0 (`tools.jackson`) |
| Maven | Wrapper incluido |
| Docker | Multi-stage build |
| Testcontainers | 1.19.7 |

---

## Arquitectura del Proyecto

```
src/main/java/prueba/microservicios/cuenta_movimientos_service/
├── CuentaMovimientosServiceApplication.java
├── config/
│   └── RabbitMQConfig.java
├── controller/
│   ├── CuentaController.java
│   ├── MovimientoController.java
│   └── ReporteController.java
├── dto/
│   ├── CuentaDTO.java
│   ├── CuentaResponseDTO.java
│   ├── EstadoCuentaDTO.java
│   ├── MovimientoDTO.java
│   ├── MovimientoReporteDTO.java
│   ├── MovimientoResponseDTO.java
│   └── ReporteDTO.java
├── entity/
│   ├── ClienteLocal.java
│   ├── Cuenta.java
│   └── Movimiento.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── SaldoNoDisponibleException.java
├── messaging/
│   └── ClienteEventConsumer.java
├── repository/
│   ├── ClienteLocalRepository.java
│   ├── CuentaRepository.java
│   └── MovimientoRepository.java
└── service/
    ├── CuentaService.java
    ├── CuentaServiceImpl.java
    ├── MovimientoService.java
    ├── MovimientoServiceImpl.java
    ├── ReporteService.java
    └── ReporteServiceImpl.java

src/test/java/prueba/microservicios/cuenta_movimientos_service/controller/
├── CuentaControllerTest.java       (9 tests)
├── MovimientoControllerTest.java   (8 tests)
└── ReporteControllerTest.java      (5 tests)
```

---

## Configuración

### Variables de Entorno

El proyecto usa variables de entorno con valores por defecto. Crea un archivo `.env` en la raíz del proyecto (ya está en `.gitignore`):

```env
# Server
SERVER_PORT=8082

# PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/cuenta_movimientos_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

### application.properties

Las propiedades sensibles se resuelven desde variables de entorno con fallback a valores por defecto:

```properties
server.port=${SERVER_PORT:8082}
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/cuenta_movimientos_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}
```

---

## Modelo de Datos

### Entidad: `Cuenta`

| Campo | Tipo | Restricciones |
|---|---|---|
| `numeroCuenta` | `String` | PK, unique, not null |
| `tipoCuenta` | `String` | not null (`Ahorro` o `Corriente`) |
| `saldoInicial` | `BigDecimal(19,2)` | not null |
| `estado` | `Boolean` | not null, default `true` |
| `clienteId` | `String` | not null (FK lógica al microservicio de clientes) |
| `movimientos` | `List<Movimiento>` | OneToMany, cascade ALL, lazy |

### Entidad: `Movimiento`

| Campo | Tipo | Restricciones |
|---|---|---|
| `id` | `Long` | PK, auto-generated (IDENTITY) |
| `fecha` | `LocalDateTime` | not null |
| `tipoMovimiento` | `String` | not null (`Deposito` o `Retiro`) |
| `valor` | `BigDecimal(19,2)` | not null |
| `saldo` | `BigDecimal(19,2)` | not null (saldo después del movimiento) |
| `cuenta` | `Cuenta` | ManyToOne, lazy, FK `numero_cuenta` |

### Entidad: `ClienteLocal`

Réplica local de datos del cliente, sincronizada vía RabbitMQ.

| Campo | Tipo | Restricciones |
|---|---|---|
| `clienteId` | `String` | PK, unique, not null |
| `nombre` | `String` | not null |
| `estado` | `Boolean` | not null, default `true` |

---

## API REST Endpoints

### Cuentas (`/cuentas`)

| Método | Endpoint | Descripción | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/cuentas` | Crear cuenta | `CuentaDTO` | `201` + `CuentaResponseDTO` |
| `GET` | `/cuentas` | Listar todas | - | `200` + `List<CuentaResponseDTO>` |
| `GET` | `/cuentas/{numeroCuenta}` | Obtener por número | - | `200` + `CuentaResponseDTO` |
| `PUT` | `/cuentas/{numeroCuenta}` | Actualizar cuenta | `CuentaDTO` | `200` + `CuentaResponseDTO` |
| `DELETE` | `/cuentas/{numeroCuenta}` | Eliminar (soft delete) | - | `204 No Content` |

#### CuentaDTO (Request)

```json
{
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorro",
  "saldoInicial": 2000.00,
  "estado": true,
  "clienteId": "CLI001"
}
```

#### CuentaResponseDTO (Response)

```json
{
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorro",
  "saldoInicial": 2000.00,
  "estado": true,
  "clienteId": "CLI001"
}
```

#### Validaciones

- `numeroCuenta`: obligatorio, no puede estar vacío
- `tipoCuenta`: obligatorio, debe ser `Ahorro` o `Corriente`
- `saldoInicial`: obligatorio, >= 0
- `clienteId`: obligatorio

---

### Movimientos (`/movimientos`)

| Método | Endpoint | Descripción | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/movimientos` | Registrar movimiento | `MovimientoDTO` | `201` + `MovimientoResponseDTO` |
| `GET` | `/movimientos` | Listar todos | - | `200` + `List<MovimientoResponseDTO>` |
| `GET` | `/movimientos/{id}` | Obtener por ID | - | `200` + `MovimientoResponseDTO` |
| `DELETE` | `/movimientos/{id}` | Eliminar movimiento | - | `204 No Content` |

#### MovimientoDTO (Request)

```json
{
  "numeroCuenta": "478758",
  "valor": 500.00
}
```

> **Nota:** Un valor positivo genera un `Deposito`, un valor negativo genera un `Retiro`.

#### MovimientoResponseDTO (Response)

```json
{
  "id": 1,
  "fecha": "2026-03-26T10:30:00",
  "tipoMovimiento": "Deposito",
  "valor": 500.00,
  "saldo": 2500.00,
  "numeroCuenta": "478758"
}
```

#### Reglas de Negocio

- El tipo de movimiento se determina automáticamente según el signo del valor
- No se permite retiros que dejen el saldo en negativo (`SaldoNoDisponibleException`)
- No se permite movimientos en cuentas inactivas
- El saldo se calcula sobre el último movimiento registrado (o saldo inicial si no hay movimientos)

---

### Reportes (`/reportes`)

| Método | Endpoint | Descripción | Query Params | Response |
|---|---|---|---|---|
| `GET` | `/reportes` | Estado de cuenta | `cliente`, `fechaInicio`, `fechaFin` | `200` + `ReporteDTO` |

#### Ejemplo de Request

```
GET /reportes?cliente=CLI001&fechaInicio=2026-03-01&fechaFin=2026-03-31
```

#### ReporteDTO (Response)

```json
{
  "cliente": "Jose Lema",
  "cuentas": [
    {
      "numeroCuenta": "478758",
      "tipo": "Ahorro",
      "saldoInicial": 2000.00,
      "estado": true,
      "movimientos": [
        {
          "fecha": "2026-03-15",
          "tipo": "Deposito",
          "valor": 500.00,
          "saldo": 2500.00
        }
      ]
    }
  ]
}
```

---

## Comunicación Asíncrona (RabbitMQ)

### Configuración

| Propiedad | Valor |
|---|---|
| Exchange | `cliente.exchange` (TopicExchange) |
| Queue | `cliente.events.queue` (durable) |
| Routing Key | `cliente.event.#` |
| Message Format | JSON (Jackson2JsonMessageConverter) |

### Consumer: `ClienteEventConsumer`

Escucha eventos del microservicio `cliente-persona-service` y sincroniza los datos del cliente en la tabla local `clientes_local`.

**Evento esperado:**

```json
{
  "clienteId": "CLI001",
  "nombre": "Jose Lema",
  "estado": true,
  "action": "CREATED"
}
```

**Acciones soportadas:** `CREATED`, `UPDATED`, `DELETED` - Todos actualizan/crean el registro local.

---

## Manejo de Excepciones

El `GlobalExceptionHandler` maneja todas las excepciones de forma centralizada:

| Excepción | HTTP Status | Descripción |
|---|---|---|
| `ResourceNotFoundException` | `404` | Recurso no encontrado (cuenta, movimiento, cliente) |
| `SaldoNoDisponibleException` | `400` | Saldo insuficiente para retiro |
| `MethodArgumentNotValidException` | `400` | Error de validación en DTOs |
| `IllegalArgumentException` | `400` | Argumento inválido (cuenta duplicada, cuenta inactiva) |
| `Exception` | `500` | Error genérico del servidor |

### Formato de Error

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Cuenta no encontrada: 478758",
  "timestamp": "2026-03-26T10:30:00.000"
}
```

---

## Tests

### Resumen

| Clase | Tests | Cobertura |
|---|---|---|
| `CuentaControllerTest` | 9 | CRUD completo + validaciones + errores |
| `MovimientoControllerTest` | 8 | Registro + listado + retiro + saldo insuficiente + cuenta no encontrada |
| `ReporteControllerTest` | 5 | Reporte exitoso + cliente no encontrado + sin parámetros + sin movimientos |
| **Total** | **22** | **Todos pasan** |

### Tecnologías de Testing

- `@WebMvcTest` (Spring Boot 4.x - paquete `org.springframework.boot.webmvc.test.autoconfigure`)
- `@MockitoBean` (Spring Boot 4.x - paquete `org.springframework.test.context.bean.override.mockito`)
- `MockMvc` para pruebas de endpoints HTTP
- `ObjectMapper` de Jackson 3.x (`tools.jackson.databind`)

### Ejecutar Tests

```bash
./mvnw test
```

Para ejecutar solo los tests de controladores:

```bash
./mvnw test -Dtest="CuentaControllerTest,MovimientoControllerTest,ReporteControllerTest"
```

---

## Docker

### Build

```bash
docker build -t cuenta-movimientos-service .
```

### Run

```bash
docker run -d \
  -p 8082:8082 \
  -e DB_URL=jdbc:postgresql://host:5432/cuenta_movimientos_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e RABBITMQ_HOST=rabbitmq \
  -e RABBITMQ_PORT=5672 \
  -e RABBITMQ_USERNAME=guest \
  -e RABBITMQ_PASSWORD=guest \
  cuenta-movimientos-service
```

---

## Levantar el Proyecto Localmente

### Prerrequisitos

- Java 17+
- PostgreSQL corriendo en `localhost:5432` con base de datos `cuenta_movimientos_db`
- RabbitMQ corriendo en `localhost:5672`

### Pasos

1. Clonar el repositorio
2. Crear archivo `.env` con las variables de entorno (ver sección de Configuración)
3. Ejecutar:

```bash
./mvnw spring-boot:run
```

El servicio estará disponible en `http://localhost:8082`.

---

## Repositorios JPA

| Repositorio | Entidad | Métodos Personalizados |
|---|---|---|
| `CuentaRepository` | `Cuenta` | `findByNumeroCuenta`, `findByClienteId`, `existsByNumeroCuenta` |
| `MovimientoRepository` | `Movimiento` | `findByCuentaNumeroCuentaOrderByFechaDesc`, `findTopByCuentaNumeroCuentaOrderByFechaDesc`, `findByCuentaAndFechaBetween` (JPQL), `findByClienteIdAndFechaBetween` (JPQL) |
| `ClienteLocalRepository` | `ClienteLocal` | `findByClienteId` |
