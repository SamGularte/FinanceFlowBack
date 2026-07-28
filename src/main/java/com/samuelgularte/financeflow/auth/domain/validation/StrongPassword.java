package com.samuelgularte.financeflow.auth.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface StrongPassword {
    String message() default "Password must be at least 8 characters with uppercase, lowercase, number and special character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
