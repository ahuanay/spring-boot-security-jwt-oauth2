package com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Table(name = "rol_permisos")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RolPermisoEntity.RolPermisoId.class)
public class RolPermisoEntity {

    @Id
    @Column(name = "rol_id")
    private Long rolId;

    @Id
    @Column(name = "permiso_id")
    private Long permisoId;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class RolPermisoId implements Serializable {

        private Long rolId;

        private Long permisoId;
    }
}
