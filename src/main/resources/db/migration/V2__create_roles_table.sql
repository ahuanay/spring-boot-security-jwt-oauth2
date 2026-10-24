-- =========================================
-- TABLA: roles
-- =========================================
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

COMMENT ON TABLE roles IS 'Almacena los roles del sistema que agrupan un conjunto de permisos.';
COMMENT ON COLUMN roles.id IS 'Identificador único auto incremental del rol.';
COMMENT ON COLUMN roles.nombre IS 'Nombre único del rol (ej. ADMIN, DOCTOR).';
COMMENT ON COLUMN roles.created_at IS 'Fecha y hora de creación del registro.';
COMMENT ON COLUMN roles.updated_at IS 'Fecha y hora de última actualización del registro.';
COMMENT ON COLUMN roles.created_by IS 'Usuario o sistema que creó el registro.';
COMMENT ON COLUMN roles.updated_by IS 'Usuario o sistema que realizó la última actualización del registro.';

-- =========================================
-- TABLA: rol_permisos (Relación ManyToMany)
-- =========================================
CREATE TABLE rol_permisos (
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_permiso FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
);

COMMENT ON TABLE rol_permisos IS 'Tabla intermedia que gestiona la relación de muchos a muchos entre roles y permisos.';
COMMENT ON COLUMN rol_permisos.rol_id IS 'Identificador del rol. Referencia a la columna roles(id).';
COMMENT ON COLUMN rol_permisos.permiso_id IS 'Identificador del permiso. Referencia a la columna permisos(id).';

-- =========================================
-- DATA INICIAL
-- =========================================
-- Inserción de Roles
INSERT INTO roles (nombre, created_by, updated_by) VALUES ('ADMIN', 'SYSTEM', 'SYSTEM');
INSERT INTO roles (nombre, created_by, updated_by) VALUES ('DOCTOR', 'SYSTEM', 'SYSTEM');
INSERT INTO roles (nombre, created_by, updated_by) VALUES ('PACIENTE', 'SYSTEM', 'SYSTEM');

-- Asignación de Permisos al Rol ADMIN (id: 1)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 1);
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 2);
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 3);
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 4);
-- Asignación de permisos ROL_* al Rol ADMIN (ids: 5–8)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 5);
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 6);
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 7);
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 8);

-- Asignación de Permisos al Rol DOCTOR (id: 2)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (2, 1);
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (2, 2);

-- Asignación de Permisos al Rol PACIENTE (id: 3)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (3, 1);