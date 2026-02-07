# 📬 Colecciones Postman - Sistema BCRP Microservicios

## 📥 Cómo Importar en Postman

### Opción 1: Importar Archivo por Archivo
1. Abre **Postman**
2. Click en **Import** (esquina superior izquierda)
3. Arrastra los 5 archivos `.json` a la ventana
4. Click **Import**

### Opción 2: Importar Todos a la Vez
1. Abre **Postman**
2. Click en **Import**
3. Selecciona **Multiple files**
4. Selecciona los 5 archivos:
   - `customer-service.postman_collection.json`
   - `account-service.postman_collection.json`
   - `bcrp-simulator.postman_collection.json`
   - `transaction-service.postman_collection.json`
   - `audit-service.postman_collection.json`
5. Click **Import**

---

## 🎯 Orden de Ejecución para Prueba Completa

### ✅ FASE 1: Crear Datos Maestros

**1. Customer Service:**
- Ejecutar: `1. Crear Cliente - Juan Perez`
- Ejecutar: `2. Crear Cliente - Maria Garcia`
- Verificar: `4. Listar Todos los Clientes`

**2. Account Service:**
- Ejecutar: `1. Crear Cuenta - Juan (Ahorros PEN)` → Cuenta 1234567890 con S/5000
- Ejecutar: `2. Crear Cuenta - Juan (Corriente USD)` → Cuenta 1234567891 con $2000
- Ejecutar: `3. Crear Cuenta - Maria (Ahorros PEN)` → Cuenta 9876543210 con S/10000
- Verificar: `4. Listar Todas las Cuentas`

---

### ✅ FASE 2: Prueba de Flujo Completo (END-TO-END)

**3. BCRP Simulator:**
- Ejecutar: `2. Simular Abono - S/1000 desde BBVA`
  - Esto dispara **TODO EL FLUJO**:
    - ✅ bcrp-simulator llama a transaction-service
    - ✅ transaction-service valida cuenta en account-service (Circuit Breaker)
    - ✅ transaction-service acredita S/1000 en account-service
    - ✅ transaction-service guarda transacción en BD
    - ✅ transaction-service confirma a bcrp-simulator (Circuit Breaker)
    - ✅ transaction-service publica evento a Kafka
    - ✅ audit-service consume evento de Kafka y guarda en BD

**4. Verificar Resultados:**
- Account Service: `6. Buscar Cuenta por Número` (1234567890)
  - **Saldo debe ser S/6000** (era 5000, sumó 1000)
- Transaction Service: `4. Listar Todas las Transacciones`
  - **Debe aparecer TRANSF-001 con estado COMPLETADO**
- Audit Service: `1. Listar Todos los Logs de Auditoría`
  - **Debe aparecer evento ABONO_PROCESADO**

---

### ✅ FASE 3: Pruebas de Patrones

**A) Circuit Breaker (Protección contra fallos):**
1. **Detener account-service** (Ctrl+C en su terminal)
2. BCRP Simulator: `2. Simular Abono - S/1000 desde BBVA`
3. **Resultado esperado:** Error 503 "Cuenta no disponible"
4. Transaction Service: `6. Listar Transacciones FALLIDAS`
   - Debe aparecer la transacción con estado FALLIDO

**B) Saga Pattern (Compensación):**
1. **Detener bcrp-simulator** (Ctrl+C en su terminal)
2. Verificar saldo inicial: Account Service → `6. Buscar Cuenta por Número`
3. Transaction Service: `1. Procesar Abono - S/1000`
4. **Resultado esperado:**
   - ✅ Dinero SE acredita (S/6000 → S/7000)
   - ❌ BCRP falla
   - 🔄 SAGA compensa (S/7000 → S/6000)
   - ✅ Saldo vuelve al original
5. Verificar: Account Service → `6. Buscar Cuenta por Número`
   - **Saldo debe ser S/6000** (no cambió porque se compensó)
6. Transaction Service: `6. Listar Transacciones FALLIDAS`
   - Debe aparecer con mensaje "BCRP no disponible - Transacción revertida"

**C) API Composite (Agregación de datos):**
- Transaction Service: `8. Obtener Detalle Completo (API Composite)`
  - Combina datos de transaction + account + customer en una sola respuesta

---

### ✅ FASE 4: Operaciones CRUD Estándar

**Customer Service:**
- `5. Obtener Cliente por ID`
- `6. Buscar Cliente por DNI`
- `7. Buscar Cliente por Nombre`
- `8. Actualizar Cliente`
- `9. Eliminar Cliente (Soft Delete)`
- `10. Listar Clientes Activos`

**Account Service:**
- `8. Acreditar S/1000 a Cuenta`
- `9. Debitar S/500 de Cuenta`
- `10. Debitar - Error Saldo Insuficiente`
- `11. Eliminar Cuenta (Soft Delete)`

**Transaction Service:**
- `5. Listar Transacciones COMPLETADAS`
- `6. Listar Transacciones FALLIDAS`
- `7. Listar Transacciones por Cuenta`

**Audit Service:**
- `2. Buscar Logs por Evento - ABONO_PROCESADO`
- `3. Buscar Logs por Evento - ABONO_REVERTIDO`
- `4. Buscar Logs por Referencia`
- `5. Buscar Logs por Cuenta`

---

## 🎓 Casos de Prueba para Sustentación

### Caso 1: Flujo Exitoso Completo
**Demostrar:** Orchestrator + Circuit Breaker + Kafka
1. Ejecutar: BCRP Simulator → `2. Simular Abono - S/1000`
2. Mostrar logs en las 5 terminales
3. Verificar: Saldo actualizado, transacción guardada, evento en auditoría

### Caso 2: Compensación SAGA
**Demostrar:** Saga Pattern con compensación
1. Detener bcrp-simulator
2. Ejecutar: Transaction Service → `1. Procesar Abono - S/1000`
3. Mostrar cómo se revierte la acreditación
4. Verificar: Saldo sin cambios, transacción marcada como FALLIDA

### Caso 3: Circuit Breaker en Acción
**Demostrar:** Circuit Breaker abre después de fallos
1. Detener account-service
2. Ejecutar 5 veces: BCRP Simulator → `2. Simular Abono`
3. Mostrar cómo Circuit Breaker pasa de CLOSED → OPEN
4. Mostrar que rechaza llamadas inmediatamente (no espera timeout)

### Caso 4: API Composite
**Demostrar:** Agregación de múltiples servicios
1. Ejecutar: Transaction Service → `8. Obtener Detalle Completo`
2. Mostrar que UNA llamada devuelve datos de 3 servicios

---

## 📊 Datos de Prueba Incluidos

### Clientes:
- **Juan Perez** (ID: 1, DNI: 12345678)
- **Maria Garcia** (ID: 2, DNI: 87654321)
- **Carlos Rodriguez** (ID: 3, DNI: 45678912)

### Cuentas:
- **1234567890** → Juan, Ahorros PEN, S/5000
- **1234567891** → Juan, Corriente USD, $2000
- **9876543210** → Maria, Ahorros PEN, S/10000

### Referencias de Transacciones:
- **TRANSF-001** → S/1000 desde BBVA
- **TRANSF-002** → S/2500 desde INTERBANK
- **TRANSF-003** → $500 USD desde BCP
- **TRANSF-ERROR-001** → Cuenta inexistente (error esperado)

---

## 🚨 Troubleshooting

### "Connection refused" al ejecutar requests
- **Solución:** Verifica que el microservicio esté corriendo:
  ```
  docker ps                    # Verificar PostgreSQL y Kafka
  netstat -an | findstr 808X   # Verificar puertos (reemplaza X)
  ```

### "Cuenta no encontrada"
- **Solución:** Ejecuta primero FASE 1 (crear clientes y cuentas)

### "No se reciben eventos en audit-service"
- **Solución:** Verifica que Kafka esté corriendo:
  ```
  docker ps | findstr kafka
  ```

### "Circuit Breaker no se abre"
- **Solución:** Necesitas 4 fallos consecutivos. Ejecuta el request 4-5 veces.

---

## ✅ Checklist de Verificación

Antes de la sustentación, verifica:
- [ ] Todos los microservicios arrancan sin errores
- [ ] PostgreSQL tiene las 5 bases de datos creadas
- [ ] Kafka y Zookeeper están corriendo
- [ ] Las 5 colecciones están importadas en Postman
- [ ] Probaste el flujo completo end-to-end
- [ ] Probaste la compensación SAGA
- [ ] Probaste que Circuit Breaker se abre con fallos

---

## 🎯 Orden de Puertos

| Servicio | Puerto | URL Base |
|----------|--------|----------|
| customer-service | 8083 | http://localhost:8083 |
| transaction-service | 8084 | http://localhost:8084 |
| account-service | 8085 | http://localhost:8085 |
| bcrp-simulator | 8086 | http://localhost:8086 |
| audit-service | 8088 | http://localhost:8088 |
| PostgreSQL | 5432 | localhost:5432 |
| Kafka | 9092 | localhost:9092 |
| Zookeeper | 2181 | localhost:2181 |

---

¡Listo para sustentar! 🚀
