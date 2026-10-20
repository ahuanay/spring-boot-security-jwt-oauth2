package com.ahuanay.auth.rol.infrastructure.entrypoints.rest.mapper;

import com.ahuanay.auth.rol.application.command.RolCommand;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto.RolRestRequest;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto.RolRestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RolRestMapper {
    RolCommand toCommand(RolRestRequest request);

    RolRestResponse toRestResponse(RolResponse response);
}