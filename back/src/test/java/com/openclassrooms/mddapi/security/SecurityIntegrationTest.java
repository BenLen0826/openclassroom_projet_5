package com.openclassrooms.mddapi.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Règles de sécurité (voir {@code docs/tests/securite.md}).
 * <p>
 * Les vérifications du token utilisent une URL de contrôle non publique
 * ({@code /api/protected-check}) : avec un token valide, la sécurité laisse passer et la
 * route inexistante donne {@code 404}. Les routes métier sont vérifiées une par une
 * ({@link #everyProtectedRoute_withoutToken_returns401}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final String PROTECTED_URL = "/api/protected-check";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void protectedRoute_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void protectedRoute_withValidToken_isLetThrough() throws Exception {
        // Arrange
        User user = userRepository.save(new User("bob", "bob@mail.fr", "hash"));
        String token = jwtService.generateToken(user);

        // Act + Assert
        mockMvc.perform(get(PROTECTED_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedRoute_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL).header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoute_withExpiredToken_returns401() throws Exception {
        // Arrange : expiré depuis 1 h, au-delà des 60 s de tolérance du décodeur
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1")
                .issuedAt(now.minus(Duration.ofHours(2)))
                .expiresAt(now.minus(Duration.ofHours(1)))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        // Act + Assert
        mockMvc.perform(get(PROTECTED_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Toutes les routes métier, sauf l'inscription et la connexion : un appel direct de l'API
     * sans token (Postman, script) doit être refusé. Une route ajoutée plus tard est couverte par
     * le refus par défaut ({@link #protectedRoute_withoutToken_returns401}).
     */
    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "GET, /api/topics",
            "GET, /api/posts",
            "POST, /api/posts",
            "GET, /api/posts/1",
            "GET, /api/posts/1/comments",
            "POST, /api/posts/1/comments",
            "GET, /api/users/me",
            "PUT, /api/users/me",
            "POST, /api/users/me/subscriptions/1",
            "DELETE, /api/users/me/subscriptions/1",
    })
    void everyProtectedRoute_withoutToken_returns401(String method, String url) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoute_withTokenSignedByAnotherKey_returns401() throws Exception {
        // Arrange : token bien formé, « utilisateur 1 », valide encore 1 h, mais signé avec une
        // clé qui n'est pas celle du serveur (ce que ferait un attaquant qui fabrique son token)
        SecretKeySpec attackerKey = new SecretKeySpec(
                "une-autre-cle-secrete-de-32-caracteres!".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder attackerEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(attackerKey));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(1)))
                .build();
        String forgedToken = attackerEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        // Act + Assert : la signature ne correspond pas à la clé du serveur
        mockMvc.perform(get(PROTECTED_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + forgedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_isPublicWithoutCsrfTokenAndCreatesNoSession() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "bob", "email": "bob@mail.fr", "password": "Secret1!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void swaggerDocumentation_isPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void swaggerDocumentation_declaresBearerScheme() throws Exception {
        // Bouton « Authorize » de Swagger UI : permet d'essayer les routes protégées avec un token
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"));
    }

    @Test
    void swaggerDocumentation_marksAuthRoutesAsPublic() throws Exception {
        // Sans token dans Swagger : un token périmé saisi dans « Authorize » ferait refuser la connexion
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/auth/register'].post.security").isEmpty());
    }
}
