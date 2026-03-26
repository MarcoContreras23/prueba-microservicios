-- =============================================
-- Base de datos: cliente_persona_db
-- =============================================

-- Conectar a cliente_persona_db
\c cliente_persona_db;

-- Tabla personas (con discriminador SINGLE_TABLE para herencia)
CREATE TABLE IF NOT EXISTS personas (
    persona_id      BIGSERIAL PRIMARY KEY,
    tipo_persona    VARCHAR(31) NOT NULL DEFAULT 'CLIENTE',
    nombre          VARCHAR(255) NOT NULL,
    genero          VARCHAR(50),
    edad            INTEGER,
    identificacion  VARCHAR(50) UNIQUE,
    direccion       VARCHAR(255),
    telefono        VARCHAR(50),
    cliente_id      VARCHAR(50) UNIQUE,
    contrasena      VARCHAR(255),
    estado          BOOLEAN DEFAULT TRUE
);

-- Datos de ejemplo: Clientes
INSERT INTO personas (tipo_persona, nombre, genero, edad, identificacion, direccion, telefono, cliente_id, contrasena, estado)
VALUES
    ('CLIENTE', 'Jose Lema', 'Masculino', 30, '1234567890', 'Otavalo sn y principal', '098254785', 'cli001', '1234', TRUE),
    ('CLIENTE', 'Marianela Montalvo', 'Femenino', 28, '0987654321', 'Amazonas y NNUU', '097548965', 'cli002', '5678', TRUE),
    ('CLIENTE', 'Juan Osorio', 'Masculino', 35, '1122334455', '13 de junio y Equinoccial', '098874587', 'cli003', '1245', TRUE);


-- =============================================
-- Base de datos: cuenta_movimientos_db
-- =============================================

-- Conectar a cuenta_movimientos_db
\c cuenta_movimientos_db;

-- Tabla de clientes locales (copia local sincronizada via RabbitMQ)
CREATE TABLE IF NOT EXISTS clientes_local (
    cliente_id  VARCHAR(50) PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    estado      BOOLEAN DEFAULT TRUE
);

-- Tabla de cuentas
CREATE TABLE IF NOT EXISTS cuentas (
    numero_cuenta   VARCHAR(50) PRIMARY KEY,
    tipo_cuenta     VARCHAR(50) NOT NULL,
    saldo_inicial   DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    estado          BOOLEAN DEFAULT TRUE,
    cliente_id      VARCHAR(50) NOT NULL
);

-- Tabla de movimientos
CREATE TABLE IF NOT EXISTS movimientos (
    id              BIGSERIAL PRIMARY KEY,
    fecha           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(50) NOT NULL,
    valor           DECIMAL(19, 2) NOT NULL,
    saldo           DECIMAL(19, 2) NOT NULL,
    numero_cuenta   VARCHAR(50) NOT NULL REFERENCES cuentas(numero_cuenta)
);

-- Datos de ejemplo: Clientes locales
INSERT INTO clientes_local (cliente_id, nombre, estado)
VALUES
    ('cli001', 'Jose Lema', TRUE),
    ('cli002', 'Marianela Montalvo', TRUE),
    ('cli003', 'Juan Osorio', TRUE);

-- Datos de ejemplo: Cuentas
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES
    ('478758', 'Ahorro', 2000.00, TRUE, 'cli001'),
    ('225487', 'Corriente', 100.00, TRUE, 'cli002'),
    ('495878', 'Ahorro', 0.00, TRUE, 'cli003'),
    ('496825', 'Ahorro', 540.00, TRUE, 'cli002');

-- Datos de ejemplo: Movimientos
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, numero_cuenta)
VALUES
    ('2022-02-08 10:00:00', 'Retiro', -575.00, 1425.00, '478758'),
    ('2022-02-10 14:30:00', 'Deposito', 600.00, 700.00, '225487'),
    ('2022-02-12 09:15:00', 'Deposito', 150.00, 150.00, '495878'),
    ('2022-02-15 11:00:00', 'Retiro', -540.00, 0.00, '496825'),
    ('2022-02-20 16:45:00', 'Deposito', 300.00, 1725.00, '478758');
