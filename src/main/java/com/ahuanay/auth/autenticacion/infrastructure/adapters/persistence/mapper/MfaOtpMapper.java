package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper;

import com.ahuanay.auth.autenticacion.domain.model.MfaOtp;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.MfaOtpEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MfaOtpMapper {
    MfaOtp toDomain(MfaOtpEntity entity);

    MfaOtpEntity toEntity(MfaOtp domain);
}
