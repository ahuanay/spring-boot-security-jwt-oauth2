package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.constraint;

import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContrasenaSeguraValidator implements ConstraintValidator<ContrasenaSegura, String> {

    @Override
    public boolean isValid(String contrasena, ConstraintValidatorContext context) {
        return contrasena == null || ReglaContrasena.esSegura(contrasena);
    }
}
