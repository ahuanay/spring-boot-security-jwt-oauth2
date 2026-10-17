package com.ahuanay.auth.permiso.infrastructure.adapters.persistence.mapper;

import com.ahuanay.auth.permiso.domain.model.Permiso;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.PermisoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermisoMapper {
    Permiso toDomain(PermisoEntity entity);
    PermisoEntity toEntity(Permiso domain);
}