package com.ahuanay.auth.common.infrastructure.utils;

import com.ahuanay.auth.common.domain.exception.ParametroInvalidoException;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.stream.Collectors;

public final class JpaQueryUtils {

    private static final Set<String> DIRECCIONES_VALIDAS = Set.of("ASC", "DESC");

    private JpaQueryUtils() {
    }

    public static Sort resolverSort(String sortBy, String direction, Set<String> camposPermitidos) {
        String campo = sortBy != null ? sortBy.toLowerCase() : "id";
        String direccion = direction != null ? direction.toUpperCase() : "ASC";

        if (!camposPermitidos.contains(campo)) {
            String camposOrdenados = camposPermitidos.stream()
                .sorted()
                .collect(Collectors.joining(", "));
            throw new ParametroInvalidoException(
                "El parámetro 'sortBy' con valor '" + sortBy + "' no es válido. Valores permitidos: "
                    + camposOrdenados + "."
            );
        }

        if (!DIRECCIONES_VALIDAS.contains(direccion)) {
            throw new ParametroInvalidoException(
                "El parámetro 'direction' con valor '" + direction + "' no es válido. Valores permitidos: ASC, DESC."
            );
        }

        return Sort.by(Sort.Direction.valueOf(direccion), campo);
    }

    public static String escaparLike(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}