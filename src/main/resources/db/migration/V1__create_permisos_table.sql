-- =========================================
-- TABLA: permisos
-- =========================================
CREATE TABLE permisos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

COMMENT ON TABLE permisos IS 'Almacena los permisos atómicos del sistema para el control de acceso basado en características.';
COMMENT ON COLUMN permisos.id IS 'Identificador único auto incremental del permiso.';
COMMENT ON COLUMN permisos.nombre IS 'Nombre único que define la acción permitida (ej. PERMISO_CREATE).';
COMMENT ON COLUMN permisos.created_at IS 'Fecha y hora de creación del registro.';
COMMENT ON COLUMN permisos.updated_at IS 'Fecha y hora de última actualización del registro.';
COMMENT ON COLUMN permisos.created_by IS 'Usuario o sistema que creó el registro.';
COMMENT ON COLUMN permisos.updated_by IS 'Usuario o sistema que realizó la última actualización del registro.';

-- =========================================
-- DATA INICIAL
-- =========================================
INSERT INTO permisos (nombre, created_by, updated_by) VALUES ('PERMISO_READ', 'SYSTEM', 'SYSTEM');
INSERT INTO permisos (nombre, created_by, updated_by) VALUES ('PERMISO_CREATE', 'SYSTEM', 'SYSTEM');
INSERT INTO permisos (nombre, created_by, updated_by) VALUES ('PERMISO_UPDATE', 'SYSTEM', 'SYSTEM');
INSERT INTO permisos (nombre, created_by, updated_by) VALUES ('PERMISO_DELETE', 'SYSTEM', 'SYSTEM');
INSERT INTO permisos (nombre, created_by, updated_by) VALUES ('ROL_READ', 'SYSTEM', 'SYSTEM');
INSERT INTO permisos (nombre, created_by, updated_by) VALUES ('ROL_CREATE', 'SYSTEM', 'SYSTEM');
INSERT INTO permisos (nombre, created_by, updated_by) VALUES ('ROL_UPDATE', 'SYSTEM', 'SYSTEM');
INSERT INTO permisos (nombre, created_by, updated_by) VALUES ('ROL_DELETE', 'SYSTEM', 'SYSTEM');