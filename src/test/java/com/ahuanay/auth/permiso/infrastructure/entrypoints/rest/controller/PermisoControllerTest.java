package com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.permiso.application.command.PermisoCommand;
import com.ahuanay.auth.permiso.application.port.in.*;
import com.ahuanay.auth.permiso.application.query.PermisoQuery;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.domain.exception.PermisoDuplicadoException;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestRequest;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestResponse;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.exception.PermisoExceptionHandler;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.mapper.PermisoRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermisoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PermisoExceptionHandler.class)
class PermisoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermisoGetAllUseCase permisoGetAllUseCase;

    @MockitoBean
    private PermisoGetByIdUseCase permisoGetByIdUseCase;

    @MockitoBean
    private PermisoCreateUseCase permisoCreateUseCase;

    @MockitoBean
    private PermisoUpdateUseCase permisoUpdateUseCase;

    @MockitoBean
    private PermisoDeleteUseCase permisoDeleteUseCase;

    @MockitoBean
    private PermisoRestMapper permisoRestMapper;

    private PermisoRestRequest request;
    private PermisoResponse useCaseResponse;
    private PermisoRestResponse restResponse;

    @BeforeEach
    void setUp() {
        request = new PermisoRestRequest("PERMISO_CREATE");
        useCaseResponse = new PermisoResponse(1L, "PERMISO_CREATE");
        restResponse = PermisoRestResponse.builder()
                .id(1L)
                .nombre("PERMISO_CREATE")
                .build();
    }

    // ==========================================
    // GET ALL
    // ==========================================

    @Test
    @DisplayName("GET /api/v1/permisos - Debería listar permisos exitosamente con estado 200 OK")
    void deberiaListarPermisosExitosamente() throws Exception {
        // Given
        PageResponse<PermisoResponse> pageUseCase = new PageResponse<>(
                List.of(useCaseResponse), 0, 10, 1L, 1, true
        );

        when(permisoGetAllUseCase.getAll(any(PermisoQuery.class))).thenReturn(pageUseCase);
        when(permisoRestMapper.toRestResponse(useCaseResponse)).thenReturn(restResponse);

        // When / Then
        mockMvc.perform(get("/api/v1/permisos")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].nombre").value("PERMISO_CREATE"))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.totalElements").value(1));

        verify(permisoGetAllUseCase, times(1)).getAll(any(PermisoQuery.class));
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    @Test
    @DisplayName("GET /api/v1/permisos/{id} - Debería obtener permiso por ID exitosamente con estado 200 OK")
    void deberiaObtenerPermisoPorIdExitosamente() throws Exception {
        // Given
        Long id = 1L;
        when(permisoGetByIdUseCase.getById(id)).thenReturn(useCaseResponse);
        when(permisoRestMapper.toRestResponse(useCaseResponse)).thenReturn(restResponse);

        // When / Then
        mockMvc.perform(get("/api/v1/permisos/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("PERMISO_CREATE"));

        verify(permisoGetByIdUseCase, times(1)).getById(id);
    }

    // ==========================================
    // CREATE (POST)
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/permisos - Debería crear un permiso exitosamente con estado 201 Created")
    void deberiaCrearPermisoExitosamente() throws Exception {
        // Given
        PermisoCommand command = new PermisoCommand("PERMISO_CREATE");
        when(permisoRestMapper.toCommand(any(PermisoRestRequest.class))).thenReturn(command);
        when(permisoCreateUseCase.create(command)).thenReturn(useCaseResponse);
        when(permisoRestMapper.toRestResponse(useCaseResponse)).thenReturn(restResponse);

        // When / Then
        mockMvc.perform(post("/api/v1/permisos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("PERMISO_CREATE"));

        verify(permisoCreateUseCase, times(1)).create(command);
    }

    @Test
    @DisplayName("POST /api/v1/permisos - Debería retornar 400 Bad Request cuando el body es inválido")
    void deberiaRetornarBadRequestCuandoBodyEsInvalido() throws Exception {
        // Given
        PermisoRestRequest requestInvalido = new PermisoRestRequest("");

        // When / Then
        mockMvc.perform(post("/api/v1/permisos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
            .andExpect(status().isBadRequest());

        verify(permisoCreateUseCase, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/v1/permisos - Debería retornar 409 Conflict cuando el permiso ya existe")
    void deberiaRetornarConflictCuandoPermisoEstaDuplicado() throws Exception {
        // Given
        PermisoCommand command = new PermisoCommand("PERMISO_CREATE");
        when(permisoRestMapper.toCommand(any(PermisoRestRequest.class))).thenReturn(command);
        when(permisoCreateUseCase.create(command))
                .thenThrow(new PermisoDuplicadoException("El permiso 'PERMISO_CREATE' ya existe."));

        // When / Then
        mockMvc.perform(post("/api/v1/permisos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));

        verify(permisoCreateUseCase, times(1)).create(command);
    }

    // ==========================================
    // UPDATE (PUT)
    // ==========================================

    @Test
    @DisplayName("PUT /api/v1/permisos/{id} - Debería actualizar un permiso exitosamente con estado 200 OK")
    void deberiaActualizarPermisoExitosamente() throws Exception {
        // Given
        Long id = 1L;
        PermisoCommand command = new PermisoCommand("PERMISO_CREATE");
        when(permisoRestMapper.toCommand(any(PermisoRestRequest.class))).thenReturn(command);
        when(permisoUpdateUseCase.update(eq(id), any(PermisoCommand.class))).thenReturn(useCaseResponse);
        when(permisoRestMapper.toRestResponse(useCaseResponse)).thenReturn(restResponse);

        // When / Then
        mockMvc.perform(put("/api/v1/permisos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("PERMISO_CREATE"));

        verify(permisoUpdateUseCase, times(1)).update(eq(id), any(PermisoCommand.class));
    }

    // ==========================================
    // DELETE
    // ==========================================

    @Test
    @DisplayName("DELETE /api/v1/permisos/{id} - Debería eliminar un permiso exitosamente con estado 204 No Content")
    void deberiaEliminarPermisoExitosamente() throws Exception {
        // Given
        Long id = 1L;
        doNothing().when(permisoDeleteUseCase).delete(id);

        // When / Then
        mockMvc.perform(delete("/api/v1/permisos/{id}", id))
            .andExpect(status().isNoContent());

        verify(permisoDeleteUseCase, times(1)).delete(id);
    }
}