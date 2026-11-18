package com.openclassrooms.mddapi.dto.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void isValid_acceptsNull_becauseRequiredIsCheckedByNotBlank() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    void isValid_acceptsMinimumLengthOf8() {
        assertThat(validator.isValid("Abcdef1!", context)).isTrue();
    }

    @Test
    void isValid_acceptsMaximumLengthOf72() {
        assertThat(validator.isValid("Aa1!" + "a".repeat(68), context)).isTrue();
    }

    @Test
    void isValid_countsAccentedLetterAsSpecialCharacter() {
        assertThat(validator.isValid("Abcdef1é", context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Abcde1!",     // 7 caractères
            "Abcdefg!",    // sans chiffre
            "ABCDEF1!",    // sans minuscule
            "abcdef1!",    // sans majuscule
            "Abcdefg1"     // sans caractère spécial
    })
    void isValid_rejectsPasswordBreakingARule(String password) {
        assertThat(validator.isValid(password, context)).isFalse();
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_rejectsLengthOf73WithTheRuleMessage() {
        assertThat(validator.isValid("Aa1!" + "a".repeat(69), context)).isFalse();
        // Message de la règle (comme le front), pas « Mot de passe trop long. »
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_rejectsPasswordOver72BytesWithSpecificMessage() {
        // Arrange : 39 caractères mais 74 octets en UTF-8 (« é » = 2 octets)
        String password = "Aa1!" + "é".repeat(35);

        // Act
        boolean valid = validator.isValid(password, context);

        // Assert
        assertThat(valid).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(PasswordValidator.MAX_BYTES_MESSAGE);
    }
}
