package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Inscription de bout en bout : sécurité, validation, service, base H2 et erreurs. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    private static final String PASSWORD = "Secret1!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    private ResultActions register(String username, String email, String password) throws Exception {
        String body = """
                {"username": "%s", "email": "%s", "password": "%s"}
                """.formatted(username, email, password);
        return mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void register_returns201WithTokenAndNeverThePassword() throws Exception {
        // Act + Assert
        register("bob", "bob@mail.fr", PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(content().string(not(org.hamcrest.Matchers.containsString(PASSWORD))));
    }

    @Test
    void register_storesHashedPassword() throws Exception {
        // Act
        register("bob", "bob@mail.fr", PASSWORD).andExpect(status().isCreated());

        // Assert
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getPassword()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, users.getFirst().getPassword())).isTrue();
    }

    @Test
    void register_returns400WithFieldErrors() throws Exception {
        // Act + Assert
        register("", "pas-un-mail", "abc")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Données invalides"))
                .andExpect(jsonPath("$.errors.username").value("Le nom d'utilisateur est obligatoire."))
                .andExpect(jsonPath("$.errors.email").value("L'e-mail n'est pas valide."))
                .andExpect(jsonPath("$.errors.password").value(
                        "Le mot de passe doit contenir entre 8 et 72 caractères, dont un chiffre, "
                                + "une minuscule, une majuscule et un caractère spécial."));
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void register_returns400WhenPasswordExceeds72Bytes() throws Exception {
        // Act + Assert : 39 caractères mais 74 octets en UTF-8
        register("bob", "bob@mail.fr", "Aa1!" + "é".repeat(35))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Mot de passe trop long."));
    }

    @Test
    void register_returns400WhenBodyIsMalformed() throws Exception {
        // Act + Assert
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns409WhenEmailTakenWithOtherCase() throws Exception {
        // Arrange
        register("bob", "bob@mail.fr", PASSWORD).andExpect(status().isCreated());

        // Act + Assert
        register("alice", "  Bob@Mail.FR ", PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cet e-mail est déjà utilisé."))
                .andExpect(jsonPath("$.field").value("email"));
    }

    @Test
    void register_returns409WhenUsernameTakenWithOtherCase() throws Exception {
        // Arrange
        register("bob", "bob@mail.fr", PASSWORD).andExpect(status().isCreated());

        // Act + Assert
        register("BOB", "alice@mail.fr", PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ce nom d'utilisateur est déjà utilisé."))
                .andExpect(jsonPath("$.field").value("username"));
    }
}
