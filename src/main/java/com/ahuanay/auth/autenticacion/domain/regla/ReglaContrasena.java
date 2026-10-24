package com.ahuanay.auth.autenticacion.domain.regla;

public final class ReglaContrasena {

    public static final String PATRON = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#+._-]).{8,72}$";

    public static final String MENSAJE =
        "La contraseña debe tener entre 8 y 72 caracteres e incluir al menos una letra mayúscula, "
            + "una letra minúscula, un número y un símbolo especial.";

    private ReglaContrasena() {
    }

    public static boolean esSegura(String contrasena) {
        return contrasena != null && contrasena.matches(PATRON);
    }
}
