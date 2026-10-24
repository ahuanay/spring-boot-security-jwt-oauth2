package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper;

import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {
    Usuario toDomain(UsuarioEntity entity);

    UsuarioEntity toEntity(Usuario domain);
}