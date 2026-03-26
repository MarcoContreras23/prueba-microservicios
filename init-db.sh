#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE cliente_persona_db;
    CREATE DATABASE cuenta_movimientos_db;
EOSQL
