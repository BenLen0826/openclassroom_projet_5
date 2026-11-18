package com.openclassrooms.mddapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** L'API doit refuser de démarrer si la clé JWT est absente ou trop courte. */
class JwtPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class)
            .withPropertyValues("mdd.jwt.expiration=7d");

    @Test
    void startupFails_whenSecretIsShorterThan32Characters() {
        runner.withPropertyValues("mdd.jwt.secret=" + "a".repeat(31))
                .run(context -> assertThat(context.getStartupFailure()).isNotNull());
    }

    @Test
    void startupFails_whenSecretIsMissing() {
        runner.run(context -> assertThat(context.getStartupFailure()).isNotNull());
    }

    @Test
    void startupSucceeds_withSecretOf32Characters() {
        runner.withPropertyValues("mdd.jwt.secret=" + "a".repeat(32))
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNull();
                    assertThat(context.getBean(JwtProperties.class).expiration()).isEqualTo(Duration.ofDays(7));
                });
    }

    @Test
    void startupFails_withTheUnchangedPlaceholderOfEnvExample() throws IOException {
        // Arrange : un .env copié sans être modifié ne doit pas démarrer avec une clé connue de tous
        String placeholder = Files.readAllLines(Path.of(".env.example")).stream()
                .filter(line -> line.startsWith("JWT_SECRET="))
                .map(line -> line.substring("JWT_SECRET=".length()))
                .findFirst()
                .orElseThrow();

        // Act + Assert
        runner.withPropertyValues("mdd.jwt.secret=" + placeholder)
                .run(context -> assertThat(context.getStartupFailure()).isNotNull());
    }

    @Configuration
    @EnableConfigurationProperties(JwtProperties.class)
    static class PropertiesConfig {
    }
}
