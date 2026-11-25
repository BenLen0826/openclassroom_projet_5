package com.openclassrooms.mddapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserTest {

    @Test
    void id_returnsTheSubjectAsLong() {
        // Arrange : le sujet (« sub ») du token est l'identifiant de l'utilisateur
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();

        // Act + Assert
        assertThat(CurrentUser.id(jwt)).isEqualTo(42L);
    }
}
