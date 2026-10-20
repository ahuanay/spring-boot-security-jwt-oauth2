package com.ahuanay.auth.rol.infrastructure.entrypoints.rest.controller;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.PageRestResponse;
import com.ahuanay.auth.rol.application.port.in.*;
import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto.RolRestRequest;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto.RolRestResponse;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.mapper.RolRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Endpoints para la gestión y administración de roles y sus permisos asociados")
public class RolController {

    private final RolGetAllUseCase rolGetAllUseCase;
    private final RolGetByIdUseCase rolGetByIdUseCase;
    private final RolCreateUseCase rolCreateUseCase;
    private final RolUpdateUseCase rolUpdateUseCase;
    private final RolDeleteUseCase rolDeleteUseCase;

    private final RolRestMapper rolRestMapper;

    @GetMapping
    @Operation(summary = "Listar roles", description = "Obtiene una lista paginada y filtrada de los roles del sistema (Solo Admin).")
    public ResponseEntity<PageRestResponse<RolRestResponse>> getAll(
        @RequestParam(required = false) String nombre,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        RolQuery query = RolQuery.builder()
            .nombre(nombre)
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .direction(direction)
            .build();

        PageResponse<RolResponse> pageResponse = rolGetAllUseCase.getAll(query);

        List<RolRestResponse> restContent = pageResponse.content().stream()
            .map(rolRestMapper::toRestResponse)
            .toList();

        PageRestResponse<RolRestResponse> response = new PageRestResponse<>(
            restContent,
            pageResponse.pageNumber(),
            pageResponse.pageSize(),
            pageResponse.totalElements(),
            pageResponse.totalPages(),
            pageResponse.last()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID", description = "Busca y devuelve los detalles de un rol específico mediante su identificador (Solo Admin).")
    public ResponseEntity<RolRestResponse> getById(@PathVariable Long id) {
        RolResponse rol = rolGetByIdUseCase.getById(id);
        return ResponseEntity.ok(rolRestMapper.toRestResponse(rol));
    }

    @PostMapping
    @Operation(summary = "Crear rol", description = "Crea un nuevo rol asignándole un conjunto de permisos (Solo Admin).")
    public ResponseEntity<RolRestResponse> create(@Valid @RequestBody RolRestRequest request) {
        RolResponse response = rolCreateUseCase.create(rolRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(rolRestMapper.toRestResponse(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol", description = "Modifica el nombre y/o los permisos asociados a un rol existente (Solo Admin).")
    public ResponseEntity<RolRestResponse> update(@PathVariable Long id, @Valid @RequestBody RolRestRequest request) {
        RolResponse response = rolUpdateUseCase.update(id, rolRestMapper.toCommand(request));
        return ResponseEntity.ok(rolRestMapper.toRestResponse(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol", description = "Elimina un rol del sistema mediante su identificador único (Solo Admin).")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rolDeleteUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}