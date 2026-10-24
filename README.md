# spring-boot-security-jwt-oauth2

Servicio de Autenticación y Autorización con Spring Boot 3.5.16, Spring Security 6, JWT e integración con OAuth2, bajo
Arquitectura Hexagonal. Gestiona usuarios, roles, permisos, sesiones JWT (con refresh y rotación), login social
(Google/GitHub) y recuperación de contraseña.

## Tecnologías

| Tecnología          | Versión | Uso                                                      |
|---------------------|---------|----------------------------------------------------------|
| Java                | 21      | Lenguaje base                                            |
| Spring Boot         | 3.5.16  | Web, Security, OAuth2 Client, Data JPA, Validation, Mail |
| JJWT                | 0.13.0  | Firma y validación de tokens JWT (HS256)                 |
| PostgreSQL + Flyway | 16 / —  | Base de datos relacional, migraciones y datos iniciales  |
| springdoc-openapi   | 2.8.4   | Documentación de la API (Swagger UI)                     |
| MapStruct           | 1.6.3   | Mapeo Entity ⇄ Domain ⇄ DTO                              |
| Docker + Compose    | —       | Orquestación (app + PostgreSQL + MailDev)                |

También: Lombok, JUnit 5 + Mockito + AssertJ (pruebas con patrón AAA) y JaCoCo (cobertura).

## Ejecución

Requisitos: Java 21, Maven (o Docker); PostgreSQL queda incluido en compose.

```bash
# 1. Plantillas de variables de entorno
cp .env.docker.example .env.docker   # para docker compose
cp .env.example .env                 # para ejecución directa con Maven

# 2. Levantar todo el stack (PostgreSQL + MailDev + la app)
docker compose up --build

# Alternativa: solo la infraestructura y correr la app con Maven
docker compose -f compose.dev.yaml up

# Ejecución directa (requiere PostgreSQL local y .env cargado)
mvn spring-boot:run
```

- API: `http://localhost:8081` — Swagger: **http://localhost:8081/swagger-ui.html**.
- Si `MAIL_MODE=smtp` con `SMTP_HOST=maildev`, los emails de recuperación/OTP llegan a MailDev:
  **http://localhost:1080**
  (SMTP en el puerto `1025`); con el modo `log` por defecto se imprimen en la consola del servicio.
- Para recrear la BD tras cambiar una migración ya aplicada: `docker compose down -v; docker compose up -d --build`.

## Endpoints

| Grupo                 | Ruta                                                                                 | Descripción                                                             |
|-----------------------|--------------------------------------------------------------------------------------|-------------------------------------------------------------------------|
| Auth (público)        | `/api/v1/auth/register`, `/login`, `/refresh`, `/forgot-password`, `/reset-password` | Registro, login dual, renovación de tokens y recuperación de contraseña |
| OTP 2FA (público)     | `/api/v1/auth/otp/verificar`, `/otp/reenviar`                                        | Verificar el código OTP del segundo factor y reenviarlo                 |
| 2FA (protegidos)      | `/api/v1/auth/mfa/activar`, `/mfa/confirmar`, `DELETE /api/v1/auth/mfa`              | Activar, confirmar y desactivar el doble factor por OTP de email        |
| Login social          | `/oauth2/authorization/google` y `/oauth2/authorization/github`                      | OAuth2 Google y GitHub (exentos de 2FA)                                 |
| Roles (protegidos)    | `/api/v1/roles` y `/api/v1/roles/{id}`                                               | CRUD protegido con `ROL_READ/CREATE/UPDATE/DELETE`                      |
| Permisos (protegidos) | `/api/v1/permisos` y `/api/v1/permisos/{id}`                                         | CRUD protegido con `PERMISO_READ/CREATE/UPDATE/DELETE`                  |

Todos los endpoints, bodies y códigos de respuesta están detallados en Swagger.

## Colección de Postman

En `postman/` hay una colección con los **22 requests** (`auth-service.postman_collection.json`) y el entorno local
(`auth-service-local.postman_environment.json`, base `http://localhost:8081`). Tras **Login**, **Verificar OTP** y
**Refresh**, los scripts guardan automáticamente `accessToken`, `refreshToken` y `tokenMfa`; los endpoints protegidos ya
llevan `Authorization: Bearer {{accessToken}}` (los públicos son `noauth`). El `resetToken` y el código OTP se copian de
los emails en MailDev (`http://localhost:1080`).

- **Postman Desktop**: Import → arrastra los dos archivos de `postman/` y selecciona el entorno *Auth Service - Local*.
- **CLI** (con la app arriba en `localhost:8081`):

```bash
postman collection run postman/auth-service.postman_collection.json \
  -e postman/auth-service-local.postman_environment.json
```

> Con el servicio arriba también puedes importar el OpenAPI de `http://localhost:8081/v3/api-docs`; la colección incluye
> además variables, scripts de captura de tokens y ejemplos listos para ejecutar.

## Variables de entorno

Completar en `.env` / `.env.docker` (plantillas incluidas):

```properties
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD                              # PostgreSQL
JWT_SECRET, JWT_EXPIRATION, JWT_REFRESH_EXPIRATION                           # Tokens
GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET # OAuth2
PASSWORD_RESET_EXPIRATION_MINUTES, PASSWORD_RESET_FRONTEND_URL               # Recuperación
APP_DEFAULT_ROLE # Rol default de OAuth2
MFA_OTP_EXPIRATION_MINUTES, MFA_OTP_MAX_ATTEMPTS, MFA_OTP_RESEND_COOLDOWN_SECONDS # 2FA
MAIL_MODE, SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD                # Email
```

> **`JWT_SECRET` es obligatorio, sin valor por defecto** (ni en `dev`): el servicio no arranca sin él. Genera una clave
> propia de al menos 64 caracteres (p. ej. `openssl rand -hex 32`) y nunca la commitees — se carga desde `.env`/entorno.

## Comportamientos clave

- **JWT y refresh:** Access Token (15 min dev) con claims `userId`, `roles` (prefijo `ROLE_*`) y `permisos` (sin
  prefijo). Refresh Token (1 h dev) persistido con `expires_at`, de un solo uso y con expiración validada también en
  base de datos; al cambiar la contraseña se revocan las sesiones activas del usuario.
- **Política de contraseñas** (fuente única en el dominio, `ReglaContrasena`): entre **8 y 72** caracteres (límite de
  bcrypt), con al menos una mayúscula, una minúscula, un número y un símbolo de `@$!%*?&#+-_`. Se aplica en `/register`
  y `/reset-password` (HTTP 400) y como segunda barrera en el dominio; el login y el 2FA no la aplican porque verifican
  credenciales existentes, no crean contraseñas.
- **Doble factor (2FA por OTP de email):** opt-in por usuario. El login con 2FA activado responde `mfaRequerido=true` y
  un `tokenMfa` para verificar el código de 6 dígitos enviado al email (`/otp/verificar`). El código y el token
  intermedio se guardan solo como hash SHA-256; intentos, vigencia y cooldown entre reenvíos son configurables vía
  variables `MFA_*`. Los usuarios de Google/GitHub están exentos.

## Datos iniciales (seed)

Flyway siembra los permisos `PERMISO_* / ROL_*`, los roles `ADMIN / DOCTOR / PACIENTE` y el usuario
`admin@mail.com` / `SecurePassword123*` (solo para desarrollo).

## Pruebas

```bash
mvn test        # 282 tests, patrón AAA — la build falla si la cobertura es < 80% (líneas y ramas)
mvn jacoco:report
```
