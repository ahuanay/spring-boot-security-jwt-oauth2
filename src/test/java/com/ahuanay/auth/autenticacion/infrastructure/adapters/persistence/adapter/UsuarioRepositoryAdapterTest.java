package com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.adapter;

import com.ahuanay.auth.autenticacion.domain.enums.AuthProvider;
import com.ahuanay.auth.autenticacion.domain.model.Usuario;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.entity.UsuarioEntity;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.mapper.UsuarioMapper;
import com.ahuanay.auth.autenticacion.infrastructure.adapters.persistence.repository.SpringDataUsuarioRepository;
import com.ahuanay.auth.permiso.infrastructure.adapters.persistence.entity.PermisoEntity;
import com.ahuanay.auth.rol.infrastructure.adapters.persistence.entity.RolEntity;
import com.ahuanay.auth.support.TestUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryAdapterTest {

    private static final Long ID = 1L;
    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "$2a$10$hashSimulado";
    private static final AuthProvider PROVIDER = AuthProvider.LOCAL;

    @Mock
    private SpringDataUsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioRepositoryAdapter usuarioRepositoryAdapter;

    private Usuario usuarioDomain;
    private UsuarioEntity usuarioEntity;

    @BeforeEach
    void setUp() {
        RolEntity rolEntity = new RolEntity(1L, "PACIENTE", Set.of(new PermisoEntity(1L, "PERMISO_READ")));

        usuarioDomain = TestUsuarios.local();
        usuarioEntity = new UsuarioEntity(ID, EMAIL, PROVIDER, null, PASSWORD, true, false, Set.of(rolEntity));
    }

    @Test
    @DisplayName("Debería retornar el usuario cuando se busca por un email existente")
    void deberiaRetornarUsuario_CuandoSeBuscaPorEmailExistente() {
        // Arrange
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioMapper.toDomain(usuarioEntity)).thenReturn(usuarioDomain);

        // Act
        Optional<Usuario> resultado = usuarioRepositoryAdapter.findByEmail(EMAIL);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(ID);
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.get().getPassword()).isEqualTo(PASSWORD);
        assertThat(resultado.get().getProvider()).isEqualTo(PROVIDER);
        assertThat(resultado.get().getActive()).isTrue();
        assertThat(resultado.get().getRoles()).hasSize(1);

        verify(usuarioRepository, times(1)).findByEmail(EMAIL);
        verify(usuarioMapper, times(1)).toDomain(usuarioEntity);
    }

    @Test
    @DisplayName("Debería retornar vacío cuando el email no existe")
    void deberiaRetornarVacio_CuandoElEmailNoExiste() {
        // Arrange
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act
        Optional<Usuario> resultado = usuarioRepositoryAdapter.findByEmail(EMAIL);

        // Assert
        assertThat(resultado).isEmpty();

        verify(usuarioRepository, times(1)).findByEmail(EMAIL);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    @DisplayName("Debería retornar el usuario cuando se busca por un ID existente")
    void deberiaRetornarUsuario_CuandoSeBuscaPorIdExistente() {
        // Arrange
        when(usuarioRepository.findById(ID)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioMapper.toDomain(usuarioEntity)).thenReturn(usuarioDomain);

        // Act
        Optional<Usuario> resultado = usuarioRepositoryAdapter.findById(ID);

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(ID);
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);

        verify(usuarioRepository, times(1)).findById(ID);
        verify(usuarioMapper, times(1)).toDomain(usuarioEntity);
    }

    @Test
    @DisplayName("Debería retornar vacío cuando el ID no existe")
    void deberiaRetornarVacio_CuandoElIdNoExiste() {
        // Arrange
        when(usuarioRepository.findById(ID)).thenReturn(Optional.empty());

        // Act
        Optional<Usuario> resultado = usuarioRepositoryAdapter.findById(ID);

        // Assert
        assertThat(resultado).isEmpty();

        verify(usuarioRepository, times(1)).findById(ID);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    @DisplayName("Debería retornar el usuario guardado cuando los datos son válidos")
    void deberiaRetornarUsuarioGuardado_CuandoLosDatosSonValidos() {
        // Arrange
        when(usuarioMapper.toEntity(usuarioDomain)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioMapper.toDomain(usuarioEntity)).thenReturn(usuarioDomain);

        // Act
        Usuario resultado = usuarioRepositoryAdapter.save(usuarioDomain);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(ID);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.getProvider()).isEqualTo(PROVIDER);
        assertThat(resultado.getActive()).isTrue();
        assertThat(resultado.getRoles()).hasSize(1);

        ArgumentCaptor<UsuarioEntity> entityCaptor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioMapper, times(1)).toEntity(usuarioDomain);
        verify(usuarioRepository, times(1)).save(entityCaptor.capture());
        verify(usuarioMapper, times(1)).toDomain(usuarioEntity);
        assertThat(entityCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(entityCaptor.getValue().getProvider()).isEqualTo(PROVIDER);
        assertThat(entityCaptor.getValue().getPassword()).isEqualTo(PASSWORD);
        assertThat(entityCaptor.getValue().getActive()).isTrue();
        assertThat(entityCaptor.getValue().getRoles()).hasSize(1);
    }
}