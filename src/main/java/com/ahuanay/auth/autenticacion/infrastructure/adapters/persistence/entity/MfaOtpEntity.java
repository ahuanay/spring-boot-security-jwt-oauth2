package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity;

import com.ahuanay.auth.autenticacion.domain.enums.PropositoOtp;
import com.ahuanay.auth.common.infrastructure.adapters.persistence.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mfa_otps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MfaOtpEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, columnDefinition = "TEXT")
    private String tokenHash;

    @Column(nullable = false)
    private String email;

    @Column(name = "codigo_hash", nullable = false, columnDefinition = "TEXT")
    private String codigoHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropositoOtp proposito = PropositoOtp.LOGIN;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int intentos;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
