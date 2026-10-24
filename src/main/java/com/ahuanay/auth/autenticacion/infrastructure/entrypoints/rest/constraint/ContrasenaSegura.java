package com.ahuanay.auth.autenticacion.infrastructure.entrypoints.rest.constraint;

import com.ahuanay.auth.autenticacion.domain.regla.ReglaContrasena;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContrasenaSeguraValidator.class)
public @interface ContrasenaSegura {

    String message() default ReglaContrasena.MENSAJE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
