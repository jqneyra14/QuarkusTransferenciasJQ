[ARQUITECTURA_COMPLETA.md](https://github.com/user-attachments/files/25153734/ARQUITECTURA_COMPLETA.md)
## 🎓PRESENTACIÓN


"El sistema implementa una arquitectura de microservicios con 6 servicios independientes: customer-service (Active Record), account-service (Repository), transaction-service (Orchestrator + Saga + Circuit Breaker), bcrp-simulator (Mock), audit-service (Event Consumer), y api-gateway (punto único de entrada). Usa PostgreSQL con database-per-service, Kafka para comunicación asíncrona, y está dockerizado para deployment cloud-ready."


## 📊 PATRONES UTILIZADOS

| Microservicio | Patrones Principales | Patrones Secundarios |
|---------------|---------------------|---------------------|
| **customer-service** | Active Record | Reactive, Soft Delete, DTO |
| **account-service** | Repository | Reactive, Domain Model, @WithTransaction |
| **transaction-service** | Orchestrator, Saga, Circuit Breaker | API Composite, Repository, REST Client, Event Producer, Reactive |
| **bcrp-simulator** | Mock/Simulator | REST Client, Facade |
| **audit-service** | Event-Driven (Consumer) | Repository, Message-Driven, Reactive |
| **api-gateway** | API Gateway | Reverse Proxy, Request Routing, CORS |



## 🔄 FLUJO COMPLETO: TRANSFERENCIA EXITOSA

```
┌─────────┐
│ Cliente │
└────┬────┘
     │ 1. POST /api/bcrp/simular-abono
     ↓
┌──────────────────┐
│  API Gateway     │
│    :8080         │
└────┬─────────────┘
     │ 2. Route to bcrp-simulator
     ↓
┌──────────────────┐
│ BCRP Simulator   │
│    :8086         │
└────┬─────────────┘
     │ 3. POST /api/transactions/procesar-abono
     ↓
┌─────────────────────────────────────────────────────────┐
│          Transaction-Service (Orchestrator)             │
│                                                          │
│  PASO 1: Validar cuenta                                 │
│     ├──► account-service:8085 (Circuit Breaker)         │
│     └──► ✅ Cuenta válida                               │
│                                                          │
│  PASO 2: Acreditar dinero                               │
│     ├──► account-service:8085 (Circuit Breaker + SAGA)  │
│     └──► ✅ S/1000 acreditados                          │
│                                                          │
│  PASO 3: Guardar transacción                            │
│     ├──► transaction_db (PostgreSQL)                    │
│     └──► ✅ Transacción guardada                        │
│                                                          │
│  PASO 4: Confirmar al BCRP                              │
│     ├──► bcrp-simulator:8086 (Circuit Breaker)          │
│     └──► ✅ BCRP confirmó                               │
│                                                          │
│  PASO 5: Publicar evento                                │
│     ├──► Kafka:9092 (audit-events)                      │
│     └──► ✅ Evento: ABONO_PROCESADO                     │
└────┬────────────────────────────────────────────────────┘
     │
     │ Evento Kafka
     ↓
┌──────────────────┐
│  Audit-Service   │
│    :8088         │
│                  │
│  1. Consume      │
│  2. Guarda en BD │
└──────────────────┘
```

---

## 🔄 FLUJO SAGA: COMPENSACIÓN POR FALLO

```
┌─────────────────────────────────────────────────────────┐
│          Transaction-Service (Orchestrator)             │
│                                                          │
│  PASO 1: ✅ Cuenta validada                             │
│  PASO 2: ✅ S/1000 acreditados (SAGA registra)          │
│  PASO 3: ✅ Transacción guardada                        │
│  PASO 4: ❌ BCRP no responde (Connection refused)       │
│                                                          │
│  ⚠️ SAGA COMPENSACIÓN ACTIVADA:                         │
│     1. Llama a account-service:8085/debitar             │
│     2. Revierte los S/1000 acreditados                  │
│     3. Actualiza transacción: estado=FALLIDO            │
│     4. Publica evento: ABONO_REVERTIDO                  │
│                                                          │
│  ✅ Sistema queda CONSISTENTE                           │
└─────────────────────────────────────────────────────────┘
```




# 🏗️ ARQUITECTURA COMPLETA - SISTEMA BCRP MICROSERVICIOS

## 📊 DIAGRAMA DE ARQUITECTURA

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           CLIENTE (Postman / Browser)                               │
└────────────────────────────────────┬────────────────────────────────────────────────┘
                                     │ HTTP Requests
                                     ↓
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                          🚪 API GATEWAY (Puerto 8080)                               │
│                                                                                      │
│  Patrones:                                                                           │
│  • API Gateway Pattern                                                               │
│  • Reverse Proxy                                                                     │
│  • Request Routing                                                                   │
│  • CORS Handling                                                                     │
│                                                                                      │
│  Tecnologías: Quarkus REST, REST Client, OpenAPI                                    │
└────┬────────────┬──────────────┬──────────────┬──────────────┬─────────────────────┘
     │            │              │              │              │
     │            │              │              │              │
     ↓            ↓              ↓              ↓              ↓
┌─────────┐ ┌──────────┐ ┌────────────┐ ┌──────────┐ ┌─────────────┐
│customer │ │ account  │ │transaction │ │   bcrp   │ │    audit    │
│-service │ │ -service │ │ -service   │ │simulator │ │  -service   │
│  :8083  │ │  :8085   │ │   :8084    │ │  :8086   │ │   :8088     │
└─────────┘ └──────────┘ └────────────┘ └──────────┘ └─────────────┘
```

---

## 🔵 CUSTOMER-SERVICE (Puerto 8083)

```
┌────────────────────────────────────────────────────────────┐
│               CUSTOMER-SERVICE                              │
│                                                              │
│  📋 Responsabilidad:                                         │
│     Gestión de clientes (CRUD)                               │
│                                                              │
│  🎯 Patrones Implementados:                                  │
│     ✅ Active Record Pattern (Principal)                     │
│     ✅ Reactive Programming (Uni, Multi)                     │
│     ✅ Soft Delete Pattern                                   │
│     ✅ DTO Pattern                                           │
│                                                              │
│  🧩 Componentes:                                             │
│     • CustomerEntity (extends PanacheEntityBase)             │
│       - Métodos: persist(), findAll(), findById()            │
│       - Lógica mezclada con entity                           │
│     • CustomerResource (REST API)                            │
│     • CustomerDTO (Data Transfer)                            │
│                                                              │
│  🗄️ Base de Datos:                                          │
│     PostgreSQL: customer_db                                  │
│                                                              │
│  📡 Endpoints:                                               │
│     GET    /api/customers                                    │
│     GET    /api/customers/{id}                               │
│     POST   /api/customers                                    │
│     PUT    /api/customers/{id}                               │
│     DELETE /api/customers/{id}                               │
└────────────────────────────────────────────────────────────┘
           │
           ↓
    ┌─────────────┐
    │ customer_db │
    │ (PostgreSQL)│
    └─────────────┘
```

---

## 🟢 ACCOUNT-SERVICE (Puerto 8085)

```
┌────────────────────────────────────────────────────────────┐
│                ACCOUNT-SERVICE                              │
│                                                              │
│  📋 Responsabilidad:                                         │
│     Gestión de cuentas bancarias                             │
│     Operaciones de acreditación y débito                     │
│                                                              │
│  🎯 Patrones Implementados:                                  │
│     ✅ Repository Pattern (Principal)                        │
│     ✅ Reactive Programming                                  │
│     ✅ Domain Model Pattern                                  │
│     ✅ @WithTransaction                                      │
│                                                              │
│  🧩 Componentes:                                             │
│     • AccountEntity (solo datos)                             │
│       - Métodos de negocio: acreditar(), debitar()           │
│     • AccountRepository (lógica de persistencia)             │
│       - extends PanacheRepositoryBase                        │
│     • AccountResource (REST API)                             │
│     • AccountDTO                                             │
│                                                              │
│  🗄️ Base de Datos:                                          │
│     PostgreSQL: account_db                                   │
│                                                              │
│  📡 Endpoints:                                               │
│     GET  /api/accounts                                       │
│     GET  /api/accounts/{id}                                  │
│     GET  /api/accounts/numero/{numero}                       │
│     POST /api/accounts                                       │
│     POST /api/accounts/{id}/acreditar?monto=X                │
│     POST /api/accounts/{id}/debitar?monto=X                  │
│     DELETE /api/accounts/{id}                                │
└────────────────────────────────────────────────────────────┘
           │
           ↓
    ┌─────────────┐
    │  account_db │
    │ (PostgreSQL)│
    └─────────────┘
```

---

## 🔴 TRANSACTION-SERVICE (Puerto 8084) - ⭐ EL MÁS COMPLEJO

```
┌──────────────────────────────────────────────────────────────────────────┐
│                      TRANSACTION-SERVICE                                  │
│                                                                            │
│  📋 Responsabilidad:                                                       │
│     Orquestación de transferencias interbancarias                         │
│     Coordinación entre múltiples servicios                                │
│                                                                            │
│  🎯 Patrones Implementados (8 PATRONES):                                  │
│     ⭐⭐⭐ Orchestrator Pattern (Principal)                                │
│     ⭐⭐⭐ Saga Pattern (Compensación)                                     │
│     ⭐⭐ Circuit Breaker Pattern                                          │
│     ⭐ API Composite Pattern                                              │
│     ✅ Repository Pattern                                                 │
│     ✅ REST Client Pattern                                                │
│     ✅ Event-Driven Architecture (Producer)                               │
│     ✅ Reactive Programming                                               │
│                                                                            │
│  🧩 Componentes Principales:                                              │
│                                                                            │
│     📌 TransactionOrchestrator (Corazón del Sistema)                      │
│        Coordina 5 PASOS:                                                  │
│        ┌──────────────────────────────────────────────┐                  │
│        │ PASO 1: Validar cuenta existe                │                  │
│        │         (@CircuitBreaker)                     │                  │
│        ├──────────────────────────────────────────────┤                  │
│        │ PASO 2: Acreditar dinero                     │                  │
│        │         (@CircuitBreaker + SAGA registra)    │                  │
│        ├──────────────────────────────────────────────┤                  │
│        │ PASO 3: Guardar transacción en BD            │                  │
│        ├──────────────────────────────────────────────┤                  │
│        │ PASO 4: Confirmar al BCRP                    │                  │
│        │         (@CircuitBreaker)                     │                  │
│        │         Si FALLA → SAGA Compensación         │                  │
│        ├──────────────────────────────────────────────┤                  │
│        │ PASO 5: Publicar evento Kafka                │                  │
│        │         (Auditoría asíncrona)                │                  │
│        └──────────────────────────────────────────────┘                  │
│                                                                            │
│     📌 Circuit Breaker (Protección):                                      │
│        • validarCuentaConCircuitBreaker()                                 │
│        • acreditarConCircuitBreaker()                                     │
│        • confirmarBcrpConCircuitBreaker()                                 │
│        Config: 4 requests, 75% fallo, 5s delay                            │
│                                                                            │
│     📌 SAGA Compensación:                                                 │
│        • compensarAcreditacion()                                          │
│        • Revierte acreditación si falla PASO 4                            │
│        • Marca transacción como FALLIDO                                   │
│                                                                            │
│     📌 REST Clients (3):                                                  │
│        • AccountServiceClient                                             │
│        • CustomerServiceClient                                            │
│        • BcrpServiceClient                                                │
│                                                                            │
│     📌 Kafka Producer:                                                    │
│        • @Channel("audit-events")                                         │
│        • Publica eventos: ABONO_PROCESADO, ABONO_REVERTIDO                │
│                                                                            │
│     📌 TransactionRepository                                              │
│     📌 TransactionEntity                                                  │
│     📌 TransactionResource (REST API + API Composite)                     │
│                                                                            │
│  🗄️ Base de Datos:                                                        │
│     PostgreSQL: transaction_db                                            │
│                                                                            │
│  📡 Endpoints:                                                             │
│     POST /api/transactions/procesar-abono                                 │
│     GET  /api/transactions                                                │
│     GET  /api/transactions/{ref}/detalle-completo (API Composite)         │
└──────────────────────────────────────────────────────────────────────────┘
     │                │                  │                    │
     ↓                ↓                  ↓                    ↓
┌──────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐
│transaction│  │account-service│  │customer-service│  │bcrp-simulator  │
│    _db    │  │    :8085      │  │    :8083      │  │    :8086       │
│(PostgreSQL)│  │REST Client    │  │REST Client    │  │REST Client     │
└──────────┘  └──────────────┘  └──────────────┘  └─────────────────┘
                                                              │
                                                              ↓
                                                    ┌─────────────────┐
                                                    │  Kafka :9092    │
                                                    │  Topic:         │
                                                    │  audit-events   │
                                                    └─────────────────┘
```

---

## 🟡 BCRP-SIMULATOR-SERVICE (Puerto 8086)

```
┌────────────────────────────────────────────────────────────┐
│              BCRP-SIMULATOR-SERVICE                         │
│                                                              │
│  📋 Responsabilidad:                                         │
│     Simular comportamiento del BCRP real                     │
│     Iniciar transferencias interbancarias                    │
│                                                              │
│  🎯 Patrones Implementados:                                  │
│     ✅ Mock/Simulator Pattern (Principal)                    │
│     ✅ REST Client Pattern                                   │
│     ✅ Facade Pattern                                        │
│                                                              │
│  🧩 Componentes:                                             │
│     • BcrpResource (REST API)                                │
│     • TransactionServiceClient (REST Client)                 │
│     • DTOs: AbonoRequestDTO, ConfirmacionDTO                 │
│                                                              │
│  💡 Características:                                         │
│     • NO usa base de datos                                   │
│     • Validaciones en memoria                                │
│     • Respuestas predefinidas                                │
│     • Llama a transaction-service para procesar              │
│                                                              │
│  📡 Endpoints:                                               │
│     POST /api/bcrp/simular-abono                             │
│     POST /api/bcrp/confirmar-abono                           │
│     GET  /api/bcrp/health                                    │
└────────────────────────────────────────────────────────────┘
                    │
                    ↓ REST Call
        ┌──────────────────────┐
        │ transaction-service  │
        │       :8084          │
        └──────────────────────┘
```

---

## 🟣 AUDIT-SERVICE (Puerto 8088)

```
┌────────────────────────────────────────────────────────────┐
│                  AUDIT-SERVICE                              │
│                                                              │
│  📋 Responsabilidad:                                         │
│     Registrar eventos de auditoría                           │
│     Consumir eventos de Kafka                                │
│                                                              │
│  🎯 Patrones Implementados:                                  │
│     ⭐⭐ Event-Driven Architecture (Consumer)                │
│     ✅ Repository Pattern                                    │
│     ✅ Message-Driven Pattern                                │
│     ✅ Reactive Programming                                  │
│                                                              │
│  🧩 Componentes:                                             │
│     • AuditEventConsumer                                     │
│       - @Incoming("audit-events")                            │
│       - Procesa mensajes asíncronos                          │
│     • AuditRepository                                        │
│     • AuditEntity                                            │
│     • AuditResource (REST API para consultas)                │
│                                                              │
│  📥 Consume de Kafka:                                        │
│     Topic: audit-events                                      │
│     Formato: EVENTO|referencia|monto|cuenta|estado           │
│                                                              │
│  🗄️ Base de Datos:                                          │
│     PostgreSQL: audit_db                                     │
│                                                              │
│  📡 Endpoints:                                               │
│     GET /api/audit                                           │
│     GET /api/audit/evento/{evento}                           │
│     GET /api/audit/referencia/{referencia}                   │
│     GET /api/audit/cuenta/{numeroCuenta}                     │
└────────────────────────────────────────────────────────────┘
        ↑                              │
        │ Kafka Events                 ↓
┌─────────────────┐          ┌─────────────┐
│  Kafka :9092    │          │  audit_db   │
│  Topic:         │          │ (PostgreSQL)│
│  audit-events   │          └─────────────┘
└─────────────────┘
```

---

## 🏗️ INFRAESTRUCTURA

```
┌─────────────────────────────────────────────────────────────────────┐
│                    INFRAESTRUCTURA                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  🗄️ POSTGRESQL (Puerto 5432)                                        │
│     ┌────────────────────────────────────────────────┐              │
│     │ • customer_db    → customer-service            │              │
│     │ • account_db     → account-service             │              │
│     │ • transaction_db → transaction-service         │              │
│     │ • audit_db       → audit-service               │              │
│     └────────────────────────────────────────────────┘              │
│     Tecnología: PostgreSQL 16 Alpine                                │
│     Driver: Reactive PostgreSQL Client                              │
│     ORM: Hibernate Reactive Panache                                 │
│                                                                       │
│  📨 KAFKA (Puerto 9092)                                              │
│     ┌────────────────────────────────────────────────┐              │
│     │ Topic: audit-events                            │              │
│     │ Producer: transaction-service                  │              │
│     │ Consumer: audit-service                        │              │
│     └────────────────────────────────────────────────┘              │
│     Tecnología: Confluent Kafka 7.5.0                               │
│     Messaging: SmallRye Reactive Messaging                          │
│                                                                       │
│  🦓 ZOOKEEPER (Puerto 2181)                                          │
│     Coordinación de Kafka cluster                                   │
│                                                                       │
│  🌐 DOCKER NETWORK                                                   │
│     Red: bcrp-network (bridge)                                      │
│     DNS interno: Resolución por nombre de contenedor                │
└─────────────────────────────────────────────────────────────────────┘
```

---

---



## 📊 PUERTOS DEL SISTEMA

| Servicio | Puerto | Protocolo |
|----------|--------|-----------|
| api-gateway | 8080 | HTTP |
| customer-service | 8083 | HTTP |
| transaction-service | 8084 | HTTP |
| account-service | 8085 | HTTP |
| bcrp-simulator | 8086 | HTTP |
| audit-service | 8088 | HTTP |
| PostgreSQL | 5432 | TCP |
| Kafka | 9092 | TCP |
| Zookeeper | 2181 | TCP |

---


