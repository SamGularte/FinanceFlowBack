package com.samuelgularte.financeflow.auth.infrastructure.validation;

import com.samuelgularte.financeflow.auth.domain.validation.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true;
        }

        List<String> violations = new ArrayList<>();

        if (password.length() < 8) {
            violations.add("at least 8 characters");
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            violations.add("an uppercase letter");
        }
        if (!password.chars().anyMatch(Character::isLowerCase)) {
            violations.add("a lowercase letter");
        }
        if (!password.chars().anyMatch(Character::isDigit)) {
            violations.add("a number");
        }
        if (password.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch))) {
            violations.add("a special character");
        }

        if (violations.isEmpty()) {
            return true;
        }

        String message = violations.size() == 1
                ? "Password must contain " + violations.getFirst()
                : "Password must contain " + String.join(", ", violations.subList(0, violations.size() - 1))
                  + " and " + violations.getLast();

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

        return false;
    }
}
