package com.ahuanay.auth.common.infrastructure.entrypoints.rest.exception;

import com.ahuanay.auth.common.domain.exception.RecursoNoEncontradoException;
import com.ahuanay.auth.common.infrastructure.entrypoints.rest.dto.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler("America/Lima");
    }

    private MethodArgumentNotValidException crearExcepcionValidacion(BindingResult bindingResult)
        throws NoSuchMethodException {
        MethodParameter methodParameter = new MethodParameter(
            GlobalExceptionHandlerTest.class.getDeclaredMethod("endpointDePrueba"), -1);
        return new MethodArgumentNotValidException(methodParameter, bindingResult);
    }

    @SuppressWarnings("unused")
    private static void endpointDePrueba() {
    }

    @Test
    @DisplayName("Debería retornar 400 con los errores por campo cuando el payload presenta errores de validación")
    void deberiaRetornar400ConErroresPorCampo_CuandoElPayloadPresentaErroresDeValidacion() throws NoSuchMethodException {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "nombre", "El nombre del permiso es obligatorio"));
        bindingResult.addError(new FieldError("request", "email", "El email es obligatorio"));
        MethodArgumentNotValidException excepcion = crearExcepcionValidacion(bindingResult);

        // Act
        ResponseEntity<ErrorResponse> respuesta =
            globalExceptionHandler.handleMethodArgumentNotValid(excepcion);

        // Assert
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponse error = respuesta.getBody();
        assertThat(error).isNotNull();
        assertThat(error.status()).isEqualTo(400);
        assertThat(error.error()).isEqualTo("Bad Request");
        assertThat(error.message()).isEqualTo("Uno o más campos presentan errores de validación.");
        assertThat(error.timestamp()).isNotNull();
        assertThat(error.errors()).isNotNull().hasSize(2);
        assertThat(error.errors().get("nombre")).isEqualTo("El nombre del permiso es obligatorio");
        assertThat(error.errors().get("email")).isEqualTo("El email es obligatorio");
        assertThat(error.notFoundIds()).isNull();
    }

    @Test
    @DisplayName("Debería retornar 400 solo con el campo inválido cuando un solo campo falla la validación")
    void deberiaRetornar400ConUnSoloError_CuandoUnCampoFallaLaValidacion() throws NoSuchMethodException {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "nombre", "El nombre es obligatorio"));
        MethodArgumentNotValidException excepcion = crearExcepcionValidacion(bindingResult);

        // Act
        ResponseEntity<ErrorResponse> respuesta =
            globalExceptionHandler.handleMethodArgumentNotValid(excepcion);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().errors()).hasSize(1);
    }

    @Test
    @DisplayName("Debería retornar 400 genérico cuando el JSON está mal formado o no es legible")
    void deberiaRetornar400Generico_CuandoElJsonEstaMalFormado() {
        HttpMessageNotReadableException excepcion = new HttpMessageNotReadableException(
            "JSON parse error",
            (Throwable) null,
            mock(HttpInputMessage.class)
        );

        // Act
        ResponseEntity<ErrorResponse> respuesta = globalExceptionHandler.handleHttpMessageNotReadable(excepcion);

        // Assert
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse error = respuesta.getBody();
        assertThat(error).isNotNull();
        assertThat(error.status()).isEqualTo(400);
        assertThat(error.error()).isEqualTo("Bad Request");
        assertThat(error.message()).isEqualTo("El formato del cuerpo de la solicitud (JSON) es inválido.");
        assertThat(error.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Debería retornar 400 especificando el campo y tipo cuando ocurre un InvalidFormatException")
    void deberiaRetornar400ConDetalleDeCampo_CuandoOcurreInvalidFormatException() {
        // Arrange
        InvalidFormatException causeMock = mock(InvalidFormatException.class);
        JsonMappingException.Reference referenceMock = new JsonMappingException.Reference(null, "permisoIds");

        when(causeMock.getPath()).thenReturn(List.of(referenceMock));
        when(causeMock.getTargetType()).thenReturn((Class) Long.class);

        HttpMessageNotReadableException excepcion = new HttpMessageNotReadableException(
            "Type mismatch",
            causeMock,
            mock(org.springframework.http.HttpInputMessage.class)
        );

        // Act
        ResponseEntity<ErrorResponse> respuesta = globalExceptionHandler.handleHttpMessageNotReadable(excepcion);

        // Assert
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse error = respuesta.getBody();
        assertThat(error).isNotNull();
        assertThat(error.status()).isEqualTo(400);
        assertThat(error.error()).isEqualTo("Bad Request");
        assertThat(error.message()).isEqualTo("El campo 'permisoIds' contiene un tipo de dato inválido. Se esperaba un valor de tipo Long.");
        assertThat(error.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Debería retornar 500 cuando ocurre una excepción de ejecución no controlada")
    void deberiaRetornar500_CuandoOcurreUnaExcepcionNoControlada() {
        // Arrange
        RuntimeException excepcion = new RuntimeException("Error de base de datos simulado");

        // Act
        ResponseEntity<ErrorResponse> respuesta = globalExceptionHandler.handleGeneralException(excepcion);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse error = respuesta.getBody();
        assertThat(error).isNotNull();
        assertThat(error.status()).isEqualTo(500);
        assertThat(error.error()).isEqualTo("Internal Server Error");
        assertThat(error.message()).isEqualTo("Ocurrió un error inesperado en el servidor.");
        assertThat(error.timestamp()).isNotNull();
        assertThat(error.errors()).isNull();
        assertThat(error.notFoundIds()).isNull();
    }

    @Test
    @DisplayName("Debería retornar 500 cuando ocurre una excepción verificada no controlada")
    void deberiaRetornar500_CuandoOcurreUnaExcepcionVerificada() {
        // Arrange
        Exception excepcion = new Exception("Fallo de serialización simulado");

        // Act
        ResponseEntity<ErrorResponse> respuesta = globalExceptionHandler.handleGeneralException(excepcion);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().message()).isEqualTo("Ocurrió un error inesperado en el servidor.");
    }

    @Test
    @DisplayName("Debería retornar 404 con los IDs no encontrados cuando la excepción incluye múltiples IDs")
    void deberiaRetornar404ConIds_CuandoLaExcepcionIncluyeVariosIds() {
        // Arrange
        RecursoNoEncontradoException excepcion =
            new RecursoNoEncontradoException("No se encontraron los permisos especificados", Set.of(1L, 2L));

        // Act
        ResponseEntity<ErrorResponse> respuesta = globalExceptionHandler.handleRecursoNoEncontrado(excepcion);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse error = respuesta.getBody();
        assertThat(error).isNotNull();
        assertThat(error.status()).isEqualTo(404);
        assertThat(error.error()).isEqualTo("Not Found");
        assertThat(error.message()).isEqualTo("No se encontraron los permisos especificados");
        assertThat(error.timestamp()).isNotNull();
        assertThat(error.notFoundIds()).isEqualTo(Set.of(1L, 2L));
        assertThat(error.errors()).isNull();
    }

    @Test
    @DisplayName("Debería retornar 404 con el ID no encontrado cuando la excepción incluye un solo ID")
    void deberiaRetornar404ConId_CuandoLaExcepcionIncluyeUnSoloId() {
        // Arrange
        RecursoNoEncontradoException excepcion =
            new RecursoNoEncontradoException("El permiso no fue encontrado", 7L);

        // Act
        ResponseEntity<ErrorResponse> respuesta = globalExceptionHandler.handleRecursoNoEncontrado(excepcion);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().notFoundIds()).isEqualTo(Set.of(7L));
    }

    @Test
    @DisplayName("Debería retornar 404 sin IDs cuando la excepción no incluye IDs no encontrados")
    void deberiaRetornar404SinIds_CuandoLaExcepcionNoIncluyeIds() {
        // Arrange
        RecursoNoEncontradoException excepcion =
            new RecursoNoEncontradoException("El recurso solicitado no existe");

        // Act
        ResponseEntity<ErrorResponse> respuesta = globalExceptionHandler.handleRecursoNoEncontrado(excepcion);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse error = respuesta.getBody();
        assertThat(error).isNotNull();
        assertThat(error.message()).isEqualTo("El recurso solicitado no existe");
        assertThat(error.notFoundIds()).isNull();
        assertThat(error.errors()).isNull();
    }
}