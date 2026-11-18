package com.openclassrooms.mddapi.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Vérifie la règle du mot de passe des spécifications (voir {@link PasswordValidator}).
 * <p>
 * La valeur {@code null} est acceptée : l'obligation est portée par {@code @NotBlank}.
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Le mot de passe doit contenir entre 8 et 72 caractères, dont un chiffre, "
            + "une minuscule, une majuscule et un caractère spécial.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
