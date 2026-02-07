#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    CREATE DATABASE customer_db;
    CREATE DATABASE account_db;
    CREATE DATABASE transaction_db;
    CREATE DATABASE audit_db;
EOSQL

echo "✅ Bases de datos creadas: customer_db, account_db, transaction_db, audit_db"