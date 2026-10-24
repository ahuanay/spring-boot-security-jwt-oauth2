-- =========================================
-- TABLA: usuarios
-- =========================================
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password TEXT,
    provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

COMMENT ON TABLE usuarios IS 'Almacena la información de autenticación y estado de los usuarios del sistema de telemedicina.';
COMMENT ON COLUMN usuarios.id IS 'Identificador único auto incremental del usuario.';
COMMENT ON COLUMN usuarios.email IS 'Correo electrónico único utilizado como credencial de acceso.';
COMMENT ON COLUMN usuarios.password IS 'Contraseña cifrada mediante algoritmo seguro (BCrypt). Nulo para usuarios autenticados mediante proveedores externos.';
COMMENT ON COLUMN usuarios.provider IS 'Proveedor del servicio de autenticación (ej. LOCAL, GITHUB).';
COMMENT ON COLUMN usuarios.provider_id IS 'Identificador único de usuario otorgado por el proveedor OAuth2.';
COMMENT ON COLUMN usuarios.active IS 'Indica si el usuario se encuentra activo (true) o inactivo/suspendido (false).';
COMMENT ON COLUMN usuarios.mfa_enabled IS 'Indica si el usuario local tiene activado el doble factor de autenticación por OTP enviado al email.';
COMMENT ON COLUMN usuarios.created_at IS 'Fecha y hora de creación del registro.';
COMMENT ON COLUMN usuarios.updated_at IS 'Fecha y hora de última actualización del registro.';
COMMENT ON COLUMN usuarios.created_by IS 'Usuario o sistema que creó el registro.';
COMMENT ON COLUMN usuarios.updated_by IS 'Usuario o sistema que realizó la última actualización del registro.';

-- =========================================
-- TABLA: usuario_roles (Relación ManyToMany)
-- =========================================
CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_rol_usuario FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

COMMENT ON TABLE usuario_roles IS 'Tabla intermedia que asigna los roles correspondientes a cada usuario.';
COMMENT ON COLUMN usuario_roles.usuario_id IS 'Identificador del usuario. Referencia a la columna usuarios(id).';
COMMENT ON COLUMN usuario_roles.rol_id IS 'Identificador del rol. Referencia a la columna roles(id).';


-- =========================================
-- TABLA: refresh_tokens
-- =========================================
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

COMMENT ON TABLE refresh_tokens IS 'Almacena los tokens de actualización (Refresh Tokens) activos para la renovación de sesiones JWT.';
COMMENT ON COLUMN refresh_tokens.id IS 'Identificador único auto incremental del registro de token.';
COMMENT ON COLUMN refresh_tokens.token IS 'Cadena de texto de longitud extendida correspondiente al Refresh Token.';
COMMENT ON COLUMN refresh_tokens.email IS 'Correo electrónico del usuario al cual pertenece el token de actualización.';
COMMENT ON COLUMN refresh_tokens.expires_at IS 'Fecha y hora exacta de expiración del refresh token en el servidor.';
COMMENT ON COLUMN refresh_tokens.created_at IS 'Fecha y hora de creación del registro.';
COMMENT ON COLUMN refresh_tokens.updated_at IS 'Fecha y hora de última actualización del registro.';
COMMENT ON COLUMN refresh_tokens.created_by IS 'Usuario o sistema que creó el registro.';
COMMENT ON COLUMN refresh_tokens.updated_by IS 'Usuario o sistema que realizó la última actualización del registro.';

CREATE INDEX idx_refresh_tokens_email ON refresh_tokens(email);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- =========================================
-- TABLA: password_reset_tokens
-- =========================================
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash TEXT NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

COMMENT ON TABLE password_reset_tokens IS 'Almacena los tokens de recuperación de contraseña emitidos a los usuarios locales del sistema de telemedicina.';
COMMENT ON COLUMN password_reset_tokens.id IS 'Identificador único auto incremental del registro de token de recuperación.';
COMMENT ON COLUMN password_reset_tokens.token_hash IS 'Hash SHA-256 del token de recuperación entregado al usuario. El valor plano nunca se persiste.';
COMMENT ON COLUMN password_reset_tokens.email IS 'Correo electrónico del usuario local que solicitó la recuperación de contraseña.';
COMMENT ON COLUMN password_reset_tokens.expires_at IS 'Fecha y hora exacta de expiración del token de recuperación en el servidor.';
COMMENT ON COLUMN password_reset_tokens.used_at IS 'Fecha y hora en la que el token de recuperación fue consumido. Nulo mientras el token aún no se utiliza.';
COMMENT ON COLUMN password_reset_tokens.created_at IS 'Fecha y hora de creación del registro.';
COMMENT ON COLUMN password_reset_tokens.updated_at IS 'Fecha y hora de última actualización del registro.';
COMMENT ON COLUMN password_reset_tokens.created_by IS 'Usuario o sistema que creó el registro.';
COMMENT ON COLUMN password_reset_tokens.updated_by IS 'Usuario o sistema que realizó la última actualización del registro.';

CREATE INDEX idx_password_reset_tokens_email ON password_reset_tokens(email);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);

-- =========================================
-- TABLA: mfa_otps (Doble factor por OTP de email)
-- =========================================
CREATE TABLE mfa_otps (
    id BIGSERIAL PRIMARY KEY,
    token_hash TEXT NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    codigo_hash TEXT NOT NULL,
    proposito VARCHAR(20) NOT NULL DEFAULT 'LOGIN',
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    intentos INT NOT NULL DEFAULT 0,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

COMMENT ON TABLE mfa_otps IS 'Almacena los códigos OTP de doble factor emitidos a los usuarios locales del sistema de telemedicina.';
COMMENT ON COLUMN mfa_otps.id IS 'Identificador único auto incremental del registro de OTP.';
COMMENT ON COLUMN mfa_otps.token_hash IS 'Hash SHA-256 del token intermedio entregado al cliente cuando el login exige el segundo factor. El valor plano nunca se persiste.';
COMMENT ON COLUMN mfa_otps.email IS 'Correo electrónico del usuario local que debe verificar el código OTP.';
COMMENT ON COLUMN mfa_otps.codigo_hash IS 'Hash SHA-256 del código OTP de 6 dígitos enviado al email del usuario. El valor plano nunca se persiste.';
COMMENT ON COLUMN mfa_otps.proposito IS 'Propósito del código OTP: LOGIN (verificar el doble factor al iniciar sesión) o ACTIVACION (activar el doble factor de un usuario autenticado).';
COMMENT ON COLUMN mfa_otps.expires_at IS 'Fecha y hora exacta de expiración del código OTP en el servidor.';
COMMENT ON COLUMN mfa_otps.intentos IS 'Cantidad de intentos fallidos de verificación consumidos al momento de emitir esta fila.';
COMMENT ON COLUMN mfa_otps.used_at IS 'Fecha y hora en la que el código OTP fue verificado correctamente. Nulo mientras el código aún no se verifica.';
COMMENT ON COLUMN mfa_otps.created_at IS 'Fecha y hora de creación del registro.';
COMMENT ON COLUMN mfa_otps.updated_at IS 'Fecha y hora de última actualización del registro.';
COMMENT ON COLUMN mfa_otps.created_by IS 'Usuario o sistema que creó el registro.';
COMMENT ON COLUMN mfa_otps.updated_by IS 'Usuario o sistema que realizó la última actualización del registro.';

CREATE INDEX idx_mfa_otps_email ON mfa_otps(email);
CREATE INDEX idx_mfa_otps_expires_at ON mfa_otps(expires_at);

-- =========================================
-- DATA INICIAL
-- =========================================
-- Inserción de Usuario de prueba (Email: admin@mail.com, Contraseña: SecurePassword123*)
INSERT INTO usuarios (email, password, provider, active)
VALUES ('admin@mail.com', '$2a$10$JcIPPV2ZskCO.Yp/CHbP2uChwBfPFYqAswpMbftIBgLKI2ZGbpmAG', 'LOCAL', true);

-- Asignación de Rol ADMIN (id: 1)
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (1, 1);