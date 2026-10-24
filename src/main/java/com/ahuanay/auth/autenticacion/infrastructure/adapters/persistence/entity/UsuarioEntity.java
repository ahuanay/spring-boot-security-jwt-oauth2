package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.common.infrastructure.adapters.persistence.entity.AuditableEntity;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.entity.RolEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<RolEntity> roles = new HashSet<>();
}