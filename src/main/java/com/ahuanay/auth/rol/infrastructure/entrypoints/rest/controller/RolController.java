package com.ahuanay.auth.rol.infrastructure.entrypoints.rest.controller;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.PageRestResponse;
import com.ahuanay.auth.rol.application.port.in.*;
import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto.RolRestRequest;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto.RolRestResponse;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.mapper.RolRestMapper;
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROL_READ')")
    @Operation(summary = "Listar roles", description = "Obtiene una lista paginada y filtrada de los roles del sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de roles obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageRestResponse<RolRestResponse>> getAll(
        @Parameter(
            description = "Filtro opcional para buscar roles por coincidencia parcial en el nombre",
            example = "ADMIN"
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
            pageResponse.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROL_READ')")
    @Operation(summary = "Obtener rol por ID", description = "Busca y devuelve los detalles de un rol específico junto con sus permisos asociados, mediante su identificador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol encontrado y retornado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RolRestResponse> getById(
        @Parameter(description = "Identificador único del rol a consultar", example = "1")
        @PathVariable Long id) {
        RolResponse rol = rolGetByIdUseCase.getById(id);
        return ResponseEntity.ok(rolRestMapper.toRestResponse(rol));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROL_CREATE')")
    @Operation(summary = "Crear rol", description = "Crea un nuevo rol asignándole un conjunto de permisos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Uno o más permisos proporcionados no existen",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El rol ya existe (nombre duplicado)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RolRestResponse> create(@Valid @RequestBody RolRestRequest request) {
        RolResponse response = rolCreateUseCase.create(rolRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(rolRestMapper.toRestResponse(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROL_UPDATE')")
    @Operation(summary = "Actualizar rol", description = "Modifica el nombre y/o los permisos asociados a un rol existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos / Errores de validación",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado o algunos de los permisos proporcionados no existen",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El rol ya existe (nombre duplicado)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RolRestResponse> update(
        @Parameter(description = "Identificador único del rol a modificar", example = "1")
        @PathVariable Long id, @Valid @RequestBody RolRestRequest request) {
        RolResponse response = rolUpdateUseCase.update(id, rolRestMapper.toCommand(request));
        return ResponseEntity.ok(rolRestMapper.toRestResponse(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROL_DELETE')")
    @Operation(summary = "Eliminar rol", description = "Elimina un rol del sistema mediante su identificador único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Rol eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Rol no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado / Sin permisos suficientes",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "Identificador único del rol a eliminar", example = "1")
        @PathVariable Long id) {
        rolDeleteUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}