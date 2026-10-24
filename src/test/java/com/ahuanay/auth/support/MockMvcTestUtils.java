package com.ahuanay.auth.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public final class MockMvcTestUtils {

    public static RequestPostProcessor usuarioAutenticado(Long usuarioId) {
        Jwt principal = Jwt.withTokenValue("token-value")
            .header("alg", "HS256")
            .claim("userId", usuarioId)
            .build();
        return request -> {
            SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(principal));
            return request;
        };
    }

    public static ResultActions postJson(MockMvc mockMvc, ObjectMapper objectMapper, String url, Object body,
                                         RequestPostProcessor... postProcesadores) throws Exception {
        return mockMvc.perform(construir(post(url), objectMapper, body, postProcesadores));
    }

    public static ResultActions putJson(MockMvc mockMvc, ObjectMapper objectMapper, String url, Object body,
                                        RequestPostProcessor... postProcesadores) throws Exception {
        return mockMvc.perform(construir(put(url), objectMapper, body, postProcesadores));
    }

    public static ResultActions deleteJson(MockMvc mockMvc, ObjectMapper objectMapper, String url, Object body,
                                           RequestPostProcessor... postProcesadores) throws Exception {
        return mockMvc.perform(construir(delete(url), objectMapper, body, postProcesadores));
    }

    private static MockHttpServletRequestBuilder construir(MockHttpServletRequestBuilder builder,
                                                           ObjectMapper objectMapper, Object body,
                                                           RequestPostProcessor... postProcesadores)
        throws com.fasterxml.jackson.core.JsonProcessingException {
        builder.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        for (RequestPostProcessor postProcesador : postProcesadores) {
            builder.with(postProcesador);
        }
        return builder;
    }

    private MockMvcTestUtils() {
    }
}
