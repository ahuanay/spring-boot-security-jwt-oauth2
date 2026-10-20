package com.ahuanay.auth.rol.infrastructure.entrypoints.rest.controller;

import com.ahuanay.auth.common.application.response.PageResponse;
import com.ahuanay.auth.rol.application.command.RolCommand;
import com.ahuanay.auth.rol.application.port.in.*;
import com.ahuanay.auth.rol.application.query.RolQuery;
import com.ahuanay.auth.rol.application.response.RolResponse;
import com.ahuanay.auth.rol.domain.exception.RolDuplicadoException;
import com.ahuanay.auth.permiso.application.response.PermisoResponse;
import com.ahuanay.auth.permiso.infrastructure.entrypoints.rest.dto.PermisoRestResponse;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto.RolRestRequest;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.dto.RolRestResponse;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.exception.RolExceptionHandler;
import com.ahuanay.auth.rol.infrastructure.entrypoints.rest.mapper.RolRestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RolController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RolExceptionHandler.class)
class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RolGetAllUseCase rolGetAllUseCase;

    @MockitoBean
    private RolGetByIdUseCase rolGetByIdUseCase;

    @MockitoBean
    private RolCreateUseCase rolCreateUseCase;

    @MockitoBean
    private RolUpdateUseCase rolUpdateUseCase;

    @MockitoBean
    private RolDeleteUseCase rolDeleteUseCase;

    @MockitoBean
    private RolRestMapper rolRestMapper;

    private RolRestRequest request;
    private RolResponse useCaseResponse;
    private RolRestResponse restResponse;

    @BeforeEach
    void setUp() {
        request = new RolRestRequest("ROL_ADMIN", Set.of(1L, 2L));
        useCaseResponse = new RolResponse(1L, "ROL_ADMIN", Set.of(
            new PermisoResponse(1L, "PERMISO_CREATE")
        ));
        restResponse = RolRestResponse.builder()
            .id(1L)
            .nombre("ROL_ADMIN")
            .permisos(Set.of(PermisoRestResponse.builder().id(1L).nombre("PERMISO_CREATE").build()))
            .build();
    }

    // ==========================================
    // GET ALL
    // ==========================================

    @Test
    @DisplayName("GET /api/v1/roles - Debería listar roles exitosamente cuando se solicitan con parámetros válidos")
    void deberiaListarRolesExitosamente_CuandoSeSolicitanConParametrosValidos() throws Exception {
        // Arrange
        PageResponse<RolResponse> pageUseCase = new PageResponse<>(
            List.of(useCaseResponse), 0, 10, 1L, 1, true
        );

        when(rolGetAllUseCase.getAll(any(RolQuery.class))).thenReturn(pageUseCase);
        when(rolRestMapper.toRestResponse(useCaseResponse)).thenReturn(restResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].nombre").value("ROL_ADMIN"))
            .andExpect(jsonPath("$.content[0].permisos[0].nombre").value("PERMISO_CREATE"))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.totalElements").value(1));

        verify(rolGetAllUseCase, times(1)).getAll(any(RolQuery.class));
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    @Test
    @DisplayName("GET /api/v1/roles/{id} - Debería obtener rol por ID exitosamente cuando el ID existe")
    void deberiaObtenerRolPorIdExitosamente_CuandoElIdExiste() throws Exception {
        // Arrange
        Long id = 1L;
        when(rolGetByIdUseCase.getById(id)).thenReturn(useCaseResponse);
        when(rolRestMapper.toRestResponse(useCaseResponse)).thenReturn(restResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles/{id}", id)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("ROL_ADMIN"))
            .andExpect(jsonPath("$.permisos[0].id").value(1));

        verify(rolGetByIdUseCase, times(1)).getById(id);
    }

    // ==========================================
    // CREATE (POST)
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/roles - Debería crear un rol exitosamente cuando el body es válido")
    void deberiaCrearRolExitosamente_CuandoElBodyEsValido() throws Exception {
        // Arrange
        RolCommand command = new RolCommand("ROL_ADMIN", Set.of(1L, 2L));
        when(rolRestMapper.toCommand(any(RolRestRequest.class))).thenReturn(command);
        when(rolCreateUseCase.create(command)).thenReturn(useCaseResponse);
        when(rolRestMapper.toRestResponse(useCaseResponse)).thenReturn(restResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("ROL_ADMIN"));

        verify(rolCreateUseCase, times(1)).create(command);
    }

    @Test
    @DisplayName("POST /api/v1/roles - Debería retornar 400 Bad Request cuando el body es inválido")
    void deberiaRetornarBadRequest_CuandoElBodyEsInvalido() throws Exception {
        // Arrange
        RolRestRequest requestInvalido = new RolRestRequest("", null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
            .andExpect(status().isBadRequest());

        verify(rolCreateUseCase, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/v1/roles - Debería retornar 409 Conflict cuando el rol ya existe")
    void deberiaRetornarConflict_CuandoElRolYaExiste() throws Exception {
        // Arrange
        RolCommand command = new RolCommand("ROL_ADMIN", Set.of(1L, 2L));
        when(rolRestMapper.toCommand(any(RolRestRequest.class))).thenReturn(command);
        when(rolCreateUseCase.create(command))
            .thenThrow(new RolDuplicadoException("El rol 'ROL_ADMIN' ya existe."));

        // Act & Assert
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));

        verify(rolCreateUseCase, times(1)).create(command);
    }

    // ==========================================
    // UPDATE (PUT)
    // ==========================================

    @Test
    @DisplayName("PUT /api/v1/roles/{id} - Debería actualizar un rol exitosamente cuando los datos son válidos")
    void deberiaActualizarRolExitosamente_CuandoLosDatosSonValidos() throws Exception {
        // Arrange
        Long id = 1L;
        RolCommand command = new RolCommand("ROL_ADMIN", Set.of(1L, 2L));
        when(rolRestMapper.toCommand(any(RolRestRequest.class))).thenReturn(command);
        when(rolUpdateUseCase.update(eq(id), any(RolCommand.class))).thenReturn(useCaseResponse);
        when(rolRestMapper.toRestResponse(useCaseResponse)).thenReturn(restResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/roles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("ROL_ADMIN"));

        verify(rolUpdateUseCase, times(1)).update(eq(id), any(RolCommand.class));
    }

    // ==========================================
    // DELETE
    // ==========================================

    @Test
    @DisplayName("DELETE /api/v1/roles/{id} - Debería eliminar un rol exitosamente cuando el ID existe")
    void deberiaEliminarRolExitosamente_CuandoElIdExiste() throws Exception {
        // Arrange
        Long id = 1L;
        doNothing().when(rolDeleteUseCase).delete(id);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/roles/{id}", id))
            .andExpect(status().isNoContent());

        verify(rolDeleteUseCase, times(1)).delete(id);
    }
}
