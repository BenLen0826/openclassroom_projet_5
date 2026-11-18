package com.openclassrooms.mddapi.controller;

import com.jayway.jsonpath.JsonPath;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Profil de bout en bout : sécurité, validation, service, base H2. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    private static final String URL = "/api/users/me";
    private static final String PASSWORD = "Secret1!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private String bobToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        bobToken = register("bob", "bob@mail.fr");
    }

    /** Inscrit un compte par l'API (vrai hachage) et renvoie son token. */
    private String register(String username, String email) throws Exception {
        String body = """
                {"username": "%s", "email": "%s", "password": "%s"}
                """.formatted(username, email, PASSWORD);
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private ResultActions update(String username, String email, String password) throws Exception {
        String passwordJson = password == null ? "null" : "\"" + password + "\"";
        String body = """
                {"username": "%s", "email": "%s", "password": %s}
                """.formatted(username, email, passwordJson);
        return mockMvc.perform(put(URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions login(String identifier, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"identifier": "%s", "password": "%s"}
                        """.formatted(identifier, password)));
    }

    @Test
    void getMe_returnsProfileWithoutPassword() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.email").value("bob@mail.fr"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void updateMe_changesUsernameAndEmail() throws Exception {
        update("bobby", "  Bobby@Mail.FR ", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bobby"))
                .andExpect(jsonPath("$.email").value("bobby@mail.fr"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void updateMe_sameTokenStillWorksAfterEmailChange() throws Exception {
        // Act : changement d'e-mail
        update("bob", "nouveau@mail.fr", null).andExpect(status().isOk());

        // Assert : le token d'avant la modification reste valide (sub = id, pas l'e-mail)
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nouveau@mail.fr"));
    }

    @Test
    void updateMe_withPassword_newPasswordLogsIn_oldOneRefused() throws Exception {
        // Act
        update("bob", "bob@mail.fr", "Nouveau2?").andExpect(status().isOk());

        // Assert
        login("bob", "Nouveau2?").andExpect(status().isOk());
        login("bob", PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    void updateMe_emptyPassword_keepsOldPassword() throws Exception {
        // Act
        update("bob", "bob@mail.fr", "").andExpect(status().isOk());

        // Assert
        login("bob", PASSWORD).andExpect(status().isOk());
    }

    @Test
    void updateMe_weakPassword_returns400() throws Exception {
        update("bob", "bob@mail.fr", "abc")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").isNotEmpty());
    }

    @Test
    void updateMe_emailOfAnotherAccount_returns409() throws Exception {
        // Arrange
        register("alice", "alice@mail.fr");

        // Act + Assert
        update("bob", "ALICE@mail.fr", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.field").value("email"));
    }

    @Test
    void updateMe_ownUsernameInOtherCase_returns200() throws Exception {
        update("Bob", "bob@mail.fr", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Bob"));
    }
}
