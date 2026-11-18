package com.openclassrooms.mddapi.security;

import com.openclassrooms.mddapi.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final JwtProperties PROPERTIES =
            new JwtProperties("test-secret-key-for-unit-tests-only-32-chars-min", Duration.ofDays(7));

    private final JwtConfig jwtConfig = new JwtConfig();
    private final JwtService jwtService = new JwtService(jwtConfig.jwtEncoder(PROPERTIES), PROPERTIES);
    private final JwtDecoder jwtDecoder = jwtConfig.jwtDecoder(PROPERTIES);

    @Test
    void generateToken_containsUserIdAsSubjectAndExpiresInSevenDays() {
        // Arrange
        User user = new User("bob", "bob@mail.fr", "hash");
        ReflectionTestUtils.setField(user, "id", 42L);

        // Act
        Jwt jwt = jwtDecoder.decode(jwtService.generateToken(user));

        // Assert
        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofDays(7));
        assertThat(Duration.between(jwt.getIssuedAt(), Instant.now()).abs()).isLessThan(Duration.ofSeconds(5));
        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
    }
}
