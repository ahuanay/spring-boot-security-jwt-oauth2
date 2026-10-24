package com.ahuanay.auth.common.infrastructure.utils;

import com.ahuanay.auth.common.domain.exception.ParametroInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaQueryUtilsTest {

    private static final Set<String> CAMPOS_PERMITIDOS = Set.of("id", "nombre");

    @Test
    @DisplayName("Debería resolver el ordenamiento aceptando cualquier capitalización cuando se solicita un campo")
    void deberiaResolverOrdenamientoAceptandoCualquierCapitalizacion_CuandoSeSolicitaUnCampo() {
        // Act
        Sort sort = JpaQueryUtils.resolverSort("NOMBRE", "desc", CAMPOS_PERMITIDOS);

        // Assert
        assertThat(sort.getOrderFor("nombre")).isNotNull();
        assertThat(sort.getOrderFor("nombre").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("Debería aplicar los valores por defecto (id, ASC) cuando los parámetros llegan nulos")
    void deberiaAplicarValoresPorDefecto_CuandoLosParametrosLleganNulos() {
        // Act
        Sort sort = JpaQueryUtils.resolverSort(null, null, CAMPOS_PERMITIDOS);

        // Assert
        assertThat(sort.getOrderFor("id")).isNotNull();
        assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("Debería lanzar ParametroInvalidoException cuando el sortBy no está permitido")
    void deberiaLanzarParametroInvalidoException_CuandoElSortByNoEstaPermitido() {
        // Act & Assert
        assertThatThrownBy(() -> JpaQueryUtils.resolverSort("password", "ASC", CAMPOS_PERMITIDOS))
            .isInstanceOf(ParametroInvalidoException.class)
            .hasMessage("El parámetro 'sortBy' con valor 'password' no es válido. Valores permitidos: id, nombre.");
    }

    @Test
    @DisplayName("Debería lanzar ParametroInvalidoException cuando la dirección no es ASC ni DESC")
    void deberiaLanzarParametroInvalidoException_CuandoLaDireccionNoEsAscNiDesc() {
        // Act & Assert
        assertThatThrownBy(() -> JpaQueryUtils.resolverSort("id", "DESENTENDIDO", CAMPOS_PERMITIDOS))
            .isInstanceOf(ParametroInvalidoException.class)
            .hasMessage("El parámetro 'direction' con valor 'DESENTENDIDO' no es válido. Valores permitidos: ASC, DESC.");
    }

    @Test
    @DisplayName("Debería escapar los comodines de LIKE (% _ y \\) y retornar vacío cuando el texto es nulo")
    void deberiaEscaparComodinesDeLikeYRetornarVacio_CuandoElTextoEsNulo() {
        // Act & Assert
        assertThat(JpaQueryUtils.escaparLike("50%")).isEqualTo("50\\%");
        assertThat(JpaQueryUtils.escaparLike("pe_re")).isEqualTo("pe\\_re");
        assertThat(JpaQueryUtils.escaparLike("ruta\\pdf")).isEqualTo("ruta\\\\pdf");

        assertThat(JpaQueryUtils.escaparLike("pe_re%z\\"))
            .isEqualTo("pe\\_re\\%z\\\\");
        
        assertThat(JpaQueryUtils.escaparLike(null)).isEmpty();
    }
}