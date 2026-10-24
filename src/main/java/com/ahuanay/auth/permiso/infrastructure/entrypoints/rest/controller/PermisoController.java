package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.controller;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.PageRestResponse;
import com.ahuanay.auth.permiso.application.port.in.*;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestRequest;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestResponse;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.mapper.PermisoRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISO_READ')")
    @Operation(
        summary = "Listar permisos",
        description = "Obtiene una lista paginada y filtrada de los permisos del sistema. Soporta consulta completa si se envía size = -1"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de permisos obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageRestResponse<PermisoRestResponse>> getAll(
        @Parameter(
            description = "Filtro opcional para buscar permisos por coincidencia parcial en el nombre",
            example = "READ"
        )
        @RequestParam(required = false) String nombre,

        @Parameter(
            description = "Número de la página a consultar (inicia en 0)",
            example = "0"
        )
        @RequestParam(defaultValue = "0") int page,

        @Parameter(
            description = "Cantidad de elementos por página. Si se envía -1, se desactivará la paginación y se retornarán todos los registros disponibles.",
            example = "10"
        )
        @RequestParam(defaultValue = "10") int size,

        @Parameter(
            description = "Campo por el cual se ordenarán los resultados",
            example = "id"
        )
        @RequestParam(defaultValue = "id") String sortBy,

        @Parameter(
            description = "Dirección del ordenamiento (ASC para ascendente, DESC para descendente)",
            example = "ASC"
        )
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
            pageResponse.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISO_READ')")
    @Operation(summary = "Obtener permiso por ID", description = "Busca y devuelve los detalles de un permiso específico mediante su identificador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permiso encontrado y retornado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Permiso no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PermisoRestResponse> getById(
        @Parameter(description = "Identificador único del permiso a consultar", example = "1")
        @PathVariable Long id) {
        PermisoResponse permiso = permisoGetByIdUseCase.getById(id);

        return ResponseEntity.ok(permisoRestMapper.toRestResponse(permiso));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISO_CREATE')")
    @Operation(summary = "Crear permiso", description = "Crea un nuevo permiso atómico en el sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Permiso creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El permiso ya existe (nombre duplicado)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PermisoRestResponse> create(@Valid @RequestBody PermisoRestRequest request) {
        PermisoResponse permisoResponse = permisoCreateUseCase.create(permisoRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoRestMapper.toRestResponse(permisoResponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISO_UPDATE')")
    @Operation(summary = "Actualizar permiso", description = "Modifica el nombre de un permiso existente mediante su identificador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permiso actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Permiso no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El permiso ya existe (nombre duplicado)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PermisoRestResponse> updatePermission(
        @Parameter(description = "Identificador único del permiso a modificar", example = "1")
        @PathVariable Long id, @Valid @RequestBody PermisoRestRequest request) {
        PermisoResponse permisoResponse = permisoUpdateUseCase.update(id, permisoRestMapper.toCommand(request));
        return ResponseEntity.ok(permisoRestMapper.toRestResponse(permisoResponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISO_DELETE')")
    @Operation(summary = "Eliminar permiso", description = "Elimina un permiso del sistema mediante su identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Permiso eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Permiso no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El permiso está asignado a uno o más roles y no puede eliminarse",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletePermission(
        @Parameter(description = "Identificador único del permiso a eliminar", example = "1")
        @PathVariable Long id) {
        permisoDeleteUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}