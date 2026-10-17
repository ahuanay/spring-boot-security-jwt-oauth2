package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.mapper;

import com.ahuanay.auth.permiso.application.command.PermisoCommand;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestRequest;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermisoRestMapper {
    PermisoCommand toCommand(PermisoRestRequest request);
    PermisoRestResponse toRestResponse(PermisoResponse response);
}