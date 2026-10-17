package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.controller;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.PageRestResponse;
import com.ahuanay.auth.permiso.application.port.in.*;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestRequest;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestResponse;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.mapper.PermisoRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permisos")
@RequiredArgsConstructor
@Tag(name = "Permisos", description = "Endpoints para la gestión y administración de permisos atómicos del sistema")
public class PermisoController {

    private final PermisoGetAllUseCase permisoGetAllUseCase;
    private final PermisoGetByIdUseCase permisoGetByIdUseCase;
    private final PermisoCreateUseCase permisoCreateUseCase;
    private final PermisoUpdateUseCase permisoUpdateUseCase;
    private final PermisoDeleteUseCase permisoDeleteUseCase;

    private final PermisoRestMapper permisoRestMapper;

    @GetMapping
    @Operation(summary = "Listar permisos", description = "Obtiene una lista paginada y filtrada de los permisos del sistema (Solo Admin).")
    public ResponseEntity<PageRestResponse<PermisoRestResponse>> getAll(
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        PermisoQuery query = PermisoQuery.builder()
                .nombre(nombre)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(direction)
                .build();

        PageResponse<PermisoResponse> pageResponse = permisoGetAllUseCase.getAll(query);

        List<PermisoRestResponse> restContent = pageResponse.content().stream()
                .map(permisoRestMapper::toRestResponse)
                .toList();

        PageRestResponse<PermisoRestResponse> response = new PageRestResponse<>(
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
    @Operation(summary = "Obtener permiso por ID", description = "Busca y devuelve los detalles de un permiso específico mediante su identificador (Solo Admin).")
    public ResponseEntity<PermisoRestResponse> getById(@PathVariable Long id) {
        PermisoResponse permiso = permisoGetByIdUseCase.getById(id);

        return ResponseEntity.ok(permisoRestMapper.toRestResponse(permiso));
    }

    @PostMapping
    @Operation(summary = "Crear permiso", description = "Crea un nuevo permiso atómico en el sistema (Solo Admin).")
    public ResponseEntity<PermisoRestResponse> create(@Valid @RequestBody PermisoRestRequest request) {
        PermisoResponse permisoResponse = permisoCreateUseCase.create(permisoRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoRestMapper.toRestResponse(permisoResponse));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar permiso", description = "Modifica el nombre de un permiso existente mediante su identificador (Solo Admin).")
    public ResponseEntity<PermisoRestResponse> updatePermission(@PathVariable Long id, @Valid @RequestBody PermisoRestRequest request) {
        PermisoResponse permisoResponse = permisoUpdateUseCase.update(id, permisoRestMapper.toCommand(request));
        return ResponseEntity.ok(permisoRestMapper.toRestResponse(permisoResponse));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar permiso", description = "Elimina un permiso del sistema mediante su identificador único (Solo Admin).")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permisoDeleteUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}