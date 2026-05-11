# 🏦 BankInc – API REST de Tarjetas y Transacciones

API REST desarrollada con **Spring Boot** y **SQLite** para la administración de tarjetas débito/crédito y transacciones financieras.

---

## 📋 Tabla de contenidos

- [Descripción](#descripción)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Modelo de base de datos](#modelo-de-base-de-datos)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Endpoints](#endpoints)
- [Ejemplos de uso](#ejemplos-de-uso)
- [Manejo de errores](#manejo-de-errores)
- [Flujo de uso recomendado](#flujo-de-uso-recomendado)

---

## 📄 Descripción

BankInc es un sistema que permite administrar tarjetas y transacciones bancarias. Cada tarjeta tiene las siguientes características:

- Número de **16 dígitos** (6 del producto + 10 aleatorios)
- Fecha de vencimiento de **3 años** a partir de la creación
- Movimientos únicamente en **dólares (USD)**
- Estados: activa, bloqueada o inactiva

El proceso de emisión de una tarjeta consta de:
1. Generar el número de tarjeta a partir del `productId`
2. Activar la tarjeta
3. Recargar saldo

---

## 🛠 Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.x | Framework backend |
| Spring Data JPA | 3.x | Acceso a datos |
| Hibernate | 6.x | ORM |
| SQLite | 3.45.1 | Base de datos |
| Lombok | latest | Reducción de boilerplate |
| Springdoc OpenAPI | 2.x | Documentación Swagger |
| JUnit 5 | 5.x | Pruebas unitarias |
| Mockito | latest | Mocking en tests |

---

## 🏗 Arquitectura

El proyecto sigue una arquitectura en capas:

```
com.nexos.bankinc
├── controller/       → Recibe peticiones HTTP
├── service/          → Lógica de negocio
├── repository/       → Acceso a la base de datos
├── entity/           → Entidades JPA (tablas)
├── dto/
│   ├── request/      → Cuerpos de entrada
│   └── response/     → Cuerpos de salida
├── exception/        → Excepciones custom y handler global
└── util/             → Utilidades (generador de número de tarjeta)
```

---

## 🗄 Modelo de base de datos

### Tabla `cards`

| Columna           | Tipo | Descripción |
|-------------------|---|---|
| `card_id`         | VARCHAR(16) PK | Número de 16 dígitos |
| `product_id`      | VARCHAR(6) | Primeros 6 dígitos del número |
| `client_name`     | VARCHAR(255) | Nombre del titular |
| `expiration_date` | DATE | Fecha de vencimiento (creación + 3 años) |
| `balance`         | DECIMAL(38,2) | Saldo disponible en USD |
| `active`          | BOOLEAN | Si la tarjeta fue activada |
| `blocked`         | BOOLEAN | Si la tarjeta está bloqueada |
| `created_at`      | TIMESTAMP | Fecha de creación |

### Tabla `transactions`

| Columna | Tipo | Descripción |
|---|---|---|
| `transaction_id` | BIGINT PK | ID autoincremental |
| `card_id` | VARCHAR(16) FK | Referencia a la tarjeta |
| `price` | DECIMAL(38,2) | Valor de la compra en USD |
| `status` | VARCHAR(255) | `ACTIVE` o `ANULLED` |
| `transaction_date` | TIMESTAMP | Fecha y hora de la transacción |

---

## ✅ Requisitos previos

- Java 17 o superior
- Maven 3.8+
- No requiere instalar base de datos (SQLite se genera automáticamente)

---

## 🚀 Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/ferparra99/BankInc.git
cd BankInc
git checkout develop
```

### 2. Compilar el proyecto

```bash
.\mvnw clean install
```

### 3. Ejecutar la aplicación

```bash
.\mvnw spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.

Al iniciar por primera vez, Hibernate crea automáticamente el archivo `bankinc.db` con las tablas necesarias.

### 4. Documentación Swagger

```
http://localhost:8080/swagger-ui.html
```

---

## 📡 Endpoints

### Tarjetas — `/card`

| # | Método | Recurso | Descripción               |
|---|---|---|---------------------------|
| 1 | `GET` | `/card/{productId}/number` | Generar número de tarjeta |
| 2 | `POST` | `/card/enroll` | Activar tarjeta           |
| 3 | `DELETE` | `/card/{cardId}` | Bloquear tarjeta          |
| 4 | `POST` | `/card/balance` | Recargar saldo            |
| 5 | `GET` | `/card/balance/{cardId}` | Consultar saldo           |
| 6 | `GET` | `/card//cards` | Listar todas las tarjetas |

### Transacciones — `/transaction`

| #  | Método | Recurso                        | Descripción |
|----|---|--------------------------------|---|
| 7  | `POST` | `/transaction/purchase`        | Realizar compra |
| 8  | `GET` | `/transaction/{transactionId}` | Consultar transacción |
| 9  | `POST` | `/transaction/anulation`       | Anular transacción |
| 10 | `GET` | `/transactions`                | Listar todas las transacciones |

---

## 📦 Ejemplos de uso

###  Generar número de tarjeta

```http
GET /card/123456/number
```

**Response 200:**
```json
{
  "cardId": "2222222358437669",
  "productId": "222222",
  "clientName": "Nombre Cliente",
  "expirationDate": "2029-04-21",
  "balance": 0,
  "active": false,
  "blocked": false,
  "createdAt": "2026-04-21T22:16:03.8020063"
}
```

---

###  Activar tarjeta

```http
POST /card/enroll
Content-Type: application/json

{
  "cardId": "1234561234567890"
}
```

**Response 200:** *Tarjeta activada con éxito*

---

###  Bloquear tarjeta

```http
DELETE /card/1234561234567890
```

**Response 200:** *Tarjeta bloqueda con éxito*

---

###  Desloquear tarjeta

```http
DELETE /card/activeCard/1234561234567890
```

**Response 200:** *Tarjeta bloqueda con éxito*

---

###  Recargar saldo

```http
POST /card/balance
Content-Type: application/json

{
    "message": "Recarga exitosa",
    "cardId": "2222222358437669",
    "balance": 100000
}
```

**Response 200:** *(sin body)*

---

###  Consultar saldo

```http
GET /card/balance/1234561234567890
```

**Response 200:**
```json
{
  "cardId": "1234561234567890",
  "balance": 500.00
}
```

---

###  Realizar compra

```http
POST /transaction/purchase
Content-Type: application/json

{
  "cardId": "1234561234567890",
  "price": 100.00
}
```

**Response 200:**
```json
{
  "transactionId": 1,
  "cardId": "1234561234567890",
  "price": 100.00,
  "status": "ACTIVE",
  "transactionDate": "2024-01-15T10:30:00"
}
```

---

###  Consultar transacción

```http
GET /transaction/1
```

**Response 200:**
```json
{
  "transactionId": 1,
  "cardId": "1234561234567890",
  "price": 100.00,
  "status": "ACTIVE",
  "transactionDate": "2024-01-15T10:30:00"
}
```

---

### Anular transacción

```http
POST /transaction/anulation
Content-Type: application/json

{
  "cardId": "1234561234567890",
  "transactionId": 1
}
```

**Response 200:**
```json
{
  "transactionId": 1,
  "cardId": "1234561234567890",
  "price": 100.00,
  "status": "ANULLED",
  "transactionDate": "2024-01-15T10:30:00"
}
```

---

###  (Test) Listar todas las transacciones

```http
GET /transaction
```

**Response 200:**
```json
[
  {
    "transactionId": 1,
    "cardId": "1234561234567890",
    "price": 100.00,
    "status": "ANULLED",
    "transactionDate": "2024-01-15T10:30:00"
  }
]
```

---

###  (Test) Listar todas las tarjetas

```http
GET /cards
```

**Response 200:**
```json
[
    {
      "cardId": "1234564078825911",
      "productId": "123456",
      "clientName": "Nombre Cliente",
      "expirationDate": "2029-04-21",
      "balance": 170000,
      "active": true,
      "blocked": false,
      "createdAt": "2026-04-21T10:20:16.587"
    }
]
```

---

## ⚠️ Manejo de errores

Todos los errores retornan el siguiente formato JSON:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "message": "Descripción del error"
}
```

### Errores posibles

| Código HTTP | Mensaje | Causa |
|---|---|---|
| 404 | `Tarjeta no encontrada: {cardId}` | La tarjeta no existe |
| 403 | `La tarjeta no ha sido activada` | Se intenta operar con una tarjeta inactiva |
| 403 | `La tarjeta está bloqueada` | La tarjeta fue bloqueada por un administrador |
| 422 | `La tarjeta está vencida` | La fecha de vencimiento ya pasó |
| 422 | `Saldo insuficiente para realizar la compra` | El saldo es menor al precio |
| 404 | `Transacción no encontrada: {transactionId}` | La transacción no existe |
| 409 | `La transacción ya fue anulada` | Se intenta anular una transacción ya anulada |
| 422 | `La transacción supera las 24 horas y no puede anularse` | Límite de tiempo para anular superado |

---

## 🔄 Flujo de uso recomendado

```
1. GET  /card/{productId}/number     → Obtener número de tarjeta
2. POST /card/enroll                 → Activar la tarjeta
3. POST /card/balance                → Recargar saldo
4. POST /transaction/purchase        → Realizar una compra
5. GET  /transaction/{id}            → Consultar la transacción
6. POST /transaction/anulation       → Anular si es necesario (máx. 24h)
```

---

## 👨‍💻 Autor

Fernando Parra — [@ferparra99](https://github.com/ferparra99)
