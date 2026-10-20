package com.ahuanay.auth.rol.infrastructure.adapters.persistence.mapper;

import com.ahuanay.auth.rol.domain.model.Rol;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.entity.RolEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RolMapper {
    Rol toDomain(RolEntity entity);

    RolEntity toEntity(Rol domain);
}