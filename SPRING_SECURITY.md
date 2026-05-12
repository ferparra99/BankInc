# Sistema de Seguridad Spring Security - Bankinc

Este documento explica cómo funciona el sistema de autenticación y autorización implementado en el proyecto Bankinc.

---

## 1. Arquitectura General

El sistema utiliza **Spring Security 6** con autenticación **JWT (JSON Web Token)** en modo **stateless**.

### Características principales:
- **Autenticación stateless**: No se almacenan sesiones en el servidor
- **Tokens JWT**: Cada request incluye un token firmado digitalmente
- **Control de acceso por roles**: Diferentes permisos según el tipo de usuario
- **CSRF deshabilitado**: Apropiado para APIs REST que usan tokens

---

## 2. Flujo de Autenticación

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FLUJO COMPLETO DE SEGURIDAD                        │
└─────────────────────────────────────────────────────────────────────────────┘

    LOGIN (Obtener Token)
    ═════════════════════
    ┌────────┐                    ┌──────────────┐                    ┌─────────┐
    │ Cliente│──POST /auth/login──▶│ AuthController│──verify──▶│ UserDB │
    │        │   {user, pass}      └───────┬───────┘                    └─────────┘
    │        │                             │                                   ▲
    │        │◀───── { token } ────────────┘                                   │
    │        │                          │                                    │
    └────────┘                          ▼                                    │
                                     ┌─────────────┐                        │
                                     │ JwtService   │◀──────────────────────┘
                                     │ generateToken│
                                     └──────┬───────┘
                                            │
                                            ▼ (JWT generado)
    ════════════════════════════════════════════════════════════════════════════

    PETICIONES AUTORIZADAS (Usar Token)
    ════════════════════════════════════
    ┌────────┐                         ┌───────────────────┐
    │ Cliente│──GET /api/resource──────▶│ JwtAuthFilter     │
    │        │  Authorization:        │ (valida token)    │
    │        │  Bearer eyJhbG...       └─────────┬─────────┘
    │        │                                   │
    │        │◀──────── { data } ────────────────┘
    │        │         (200 OK)
    └────────┘
```

---

## 3. Componentes del Sistema

### 3.1 SecurityConfig.java
**Ubicación:** `src/main/java/com/nexos/bankinc/config/SecurityConfig.java`

Es el archivo principal de configuración de seguridad.

**Funciones:**
1. **Define qué endpoints son públicos y cuáles requieren autenticación**
2. **Configura el encoder de contraseñas (BCrypt)**
3. **Configura CORS para permitir peticiones cross-origin**
4. **Agrega el filtro JWT al chain de filtros de Spring Security**
5. **Establece la política de sesiones como STATELESS**

**Configuración de endpoints:**

| Método | Endpoint | Roles Permitidos | Descripción |
|--------|----------|------------------|-------------|
| POST | `/auth/login` | (público) | Iniciar sesión |
| POST | `/auth/logout` | (público) | Cerrar sesión |
| GET | `/card/{productId}/number` | ADMIN, CAJERO | Generar número de tarjeta |
| POST | `/card/enroll` | ADMIN, CAJERO, CLIENTE | Inscribir tarjeta |
| DELETE | `/card/{cardId}` | ADMIN, SUPERVISOR | Eliminar tarjeta |
| POST | `/card/balance` | ADMIN, CAJERO, CLIENTE | Recargar saldo |
| GET | `/card/balance/{cardId}` | ADMIN, CAJERO, CLIENTE | Consultar saldo |
| GET | `/card/cards` | ADMIN, SUPERVISOR | Listar tarjetas |
| DELETE | `/card/activeCard/{cardId}` | ADMIN, SUPERVISOR | Bloquear tarjeta |
| POST | `/transaction/purchase` | ADMIN, CAJERO, CLIENTE | Realizar compra |
| GET | `/transaction/{id}` | ADMIN, CAJERO, CLIENTE | Ver transacción |
| POST | `/transaction/anulation` | ADMIN, SUPERVISOR | Anular transacción |
| GET | `/transaction/transaccions` | ADMIN, SUPERVISOR | Listar transacciones |

### 3.2 JwtService.java
**Ubicación:** `src/main/java/com/nexos/bankinc/security/JwtService.java`

Maneja la creación y validación de tokens JWT.

**Métodos principales:**

| Método | Descripción |
|--------|-------------|
| `generateToken(userDetails)` | Crea un nuevo JWT con username y expiración |
| `extractUsername(token)` | Extrae el nombre de usuario del token |
| `validateToken(token, userDetails)` | Valida que el token sea auténtico y no haya expirado |

**Estructura del JWT generado:**

```
┌─────────────────────────────────────────────────────────┐
│                        JWT TOKEN                          │
├───────────────┬─────────────────┬────────────────────────┤
│     HEADER    │     PAYLOAD     │      SIGNATURE         │
├───────────────┼─────────────────┼────────────────────────┤
│ alg: HS256    │ sub: username   │ HMAC-SHA256(           │
│ typ: JWT      │ iat: timestamp  │   header.payload,     │
│               │ exp: timestamp  │   secret               │
│               │                 │ )                      │
└───────────────┴─────────────────┴────────────────────────┘
```

### 3.3 JwtAuthenticationFilter.java
**Ubicación:** `src/main/java/com/nexos/bankinc/security/JwtAuthenticationFilter.java`

Filtro que se ejecuta en cada petición HTTP para validar el token JWT.

**Flujo del filtro:**

```
1. Recebe request HTTP
        │
        ▼
2. ¿El endpoint es /auth/*? ──SÍ──▶ Continuar sin filtro (permite login)
        │
        NO
        │
        ▼
3. ¿Existe header "Authorization: Bearer <token>"?
        │
        ├──NO──▶ Continuar (el request requerirá auth manualmente)
        │
        SÍ
        │
        ▼
4. Extraer el token (remover "Bearer ")
        │
        ▼
5. Extraer username del token
        │
        ▼
6. ¿El usuario ya está autenticado en este contexto?
        │
        ├──SÍ──▶ Continuar
        │
        NO
        │
        ▼
7. Cargar usuario de la BD usando UserDetailsService
        │
        ▼
8. ¿El token es válido? (firma correcta + no expirado)
        │
        ├──NO──▶ Continuar sin auth
        │
        SÍ
        │
        ▼
9. Crear Authentication token con permisos del usuario
        │
        ▼
10. Establecer SecurityContextHolder.getContext().setAuthentication()
        │
        ▼
11. Continuar con el resto del FilterChain
```

### 3.4 AuthController.java
**Ubicación:** `src/main/java/com/nexos/bankinc/controller/AuthController.java`

Controlador REST para operaciones de autenticación.

**Endpoints:**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/login` | Inicia sesión y retorna JWT |
| POST | `/auth/logout` | Cierra sesión |

### 3.5 User.java (Entidad)
**Ubicación:** `src/main/java/com/nexos/bankinc/entity/User.java`

Entidad JPA que representa un usuario del sistema.

**Implementa:** `UserDetails` de Spring Security

**Relaciones:**
- Roles: `@ElementCollection` con tabla `user_roles`

### 3.6 Role.java (Enum)
**Ubicación:** `src/main/java/com/nexos/bankinc/security/Role.java`

Define los roles disponibles en el sistema:

```java
public enum Role {
    ROLE_ADMIN,      // Administrador - acceso total
    ROLE_CAJERO,     // Empleado - transacciones y tarjetas
    ROLE_CLIENTE,    // Cliente - acceso limitado
    ROLE_SUPERVISOR  // Supervisor - reportes y anulaciones
}
```

### 3.7 UserDetailsServiceImpl.java
**Ubicación:** `src/main/java/com/nexos/bankinc/security/UserDetailsServiceImpl.java`

Carga usuarios desde la base de datos para autenticación.

---

## 4. Cómo Usar la API

### 4.1 Iniciar Sesión

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Respuesta exitosa:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6..."
}
```

### 4.2 Usar el Token en Peticiones

```bash
curl -X GET http://localhost:8080/card/cards \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIs..."
```

### 4.3 Cerrar Sesión

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer <tu_token>"
```

**Nota:** En un sistema stateless con JWT, el logout del servidor es limitado. El cliente debe eliminar el token de su almacenamiento.

---

## 5. Usuarios de Prueba

El sistema incluye un `DataInitializer` que crea usuarios de prueba:

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ROLE_ADMIN |
| cajero | cajero123 | ROLE_CAJERO |
| cliente | cliente123 | ROLE_CLIENTE |
| supervisor | supervisor123 | ROLE_SUPERVISOR |

---

## 6. Configuración

### 6.1 application.properties

```properties
# Clave secreta para firmar tokens JWT
# IMPORTANTE: Cambiar en producción por una clave segura
jwt.secret=bankinc-secret-key-that-is-at-least-32-bytes-long-for-hs256

# Tiempo de expiración del token (en segundos)
# Por defecto: 86400 (24 horas)
jwt.expiration=86400
```

### 6.2 Recomendaciones de Seguridad

1. **Cambiar la clave JWT**: Generar una clave segura de al menos 32 caracteres
2. **Usar variables de entorno**: No hardcodear secrets en código
3. **Reducir tiempo de expiración**: Para mayor seguridad, usar tokens de corta duración
4. **Implementar refresh tokens**: Para sesiones prolongadas sin re-login
5. **Agregar rate limiting**: Prevenir ataques de fuerza bruta

---

## 7. Mejoras Futuras Sugeridas

1. **Blacklist de tokens**: Almacenar tokens revocados en Redis o BD
2. **Refresh tokens**: Tokens de larga duración para renovar el access token
3. **Rate limiting**: Limitar intentos de login
4. **Bloqueo de cuentas**: Después de X intentos fallidos
5. **Logging de seguridad**: Registrar intentos de login y accesos
6. **2FA**: Autenticación de dos factores para acciones sensibles

---

## 8. Diagramas de Seguridad

### Cadena de Filtros de Spring Security

```
┌─────────────────────────────────────────────────────────────────┐
│                    HTTP REQUEST                                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SecurityFilterChain                          │
├─────────────────────────────────────────────────────────────────┤
│  1. ChannelProcessingFilter (¿HTTP o HTTPS?)                    │
│  2. SecurityContextPersistenceFilter                            │
│  3. CorsFilter  ◀─── Configuración CORS                          │
│  4. LogoutFilter                                                 │
│  5. JwtAuthenticationFilter  ◀─── NUESTRO FILTRO JWT              │
│  6. UsernamePasswordAuthenticationFilter                         │
│  7. DefaultLoginPageGeneratingFilter                             │
│  8. FilterSecurityInterceptor (valida permisos del endpoint)       │
└─────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   CONTROLLER / SERVICIO                          │
└─────────────────────────────────────────────────────────────────┘
```

### Flujo de Autorización

```
Request llega
     │
     ▼
¿Endpoint público? ──SÍ──▶ Permitir acceso
     │
     NO
     │
     ▼
¿Token JWT válido?
     │
     ├──NO──▶ 401 Unauthorized
     │
     SÍ
     │
     ▼
¿Usuario tiene rol requerido?
     │
     ├──NO──▶ 403 Forbidden
     │
     SÍ
     │
     ▼
✅ Permitir acceso al recurso
```

---

## 9. Referencias

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/)
- [OWASP Security Guidelines](https://owasp.org/)

---

*Documento generado automáticamente para el proyecto Bankinc*