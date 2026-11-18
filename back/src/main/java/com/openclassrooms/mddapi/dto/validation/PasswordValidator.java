package com.openclassrooms.mddapi.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Règle du mot de passe : entre 8 et 72 caractères, dont au moins une minuscule,
 * une majuscule, un chiffre et un caractère spécial.
 * <p>
 * Classes de caractères (identiques au validateur du front, pour un même résultat) :
 * minuscule = {@code [a-z]}, majuscule = {@code [A-Z]}, chiffre = {@code [0-9]},
 * caractère spécial = tout ce qui n'est ni une lettre ASCII ni un chiffre
 * ({@code é} ou un espace comptent donc comme caractères spéciaux).
 * <p>
 * Maximum de 72 : BCrypt ne prend en compte que 72 octets et l'encodeur de
 * Spring Security refuse les mots de passe plus longs. Un mot de passe de 72
 * caractères au plus peut dépasser 72 octets (caractères accentués) : il est
 * refusé ici avec un message dédié, au lieu de provoquer une erreur 500.
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    public static final String MAX_BYTES_MESSAGE = "Mot de passe trop long.";

    private static final int BCRYPT_MAX_BYTES = 72;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,72}$");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return false;
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MAX_BYTES_MESSAGE).addConstraintViolation();
            return false;
        }
        return true;
    }
}
