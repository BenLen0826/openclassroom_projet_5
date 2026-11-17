package com.openclassrooms.mddapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste l'endpoint des thèmes de bout en bout : sécurité, contrôleur, service,
 * mapper et base H2 (remplie par {@code TopicDataInitializer} au démarrage).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TopicControllerIntegrationTest {

    private static final String FRONT_ORIGIN = "http://localhost:4200";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllTopics_returnsReferenceTopicsSortedByNameWithoutAuthentication() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[*].name", contains(
                        "Angular", "Bases de données", "DevOps", "Java",
                        "JavaScript", "Python", "Spring", "TypeScript")))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].description", notNullValue()));
    }

    @Test
    void cors_allowsFrontOrigin() throws Exception {
        // Act + Assert : requête de pré-vérification envoyée par le navigateur
        mockMvc.perform(options("/api/topics")
                        .header(HttpHeaders.ORIGIN, FRONT_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONT_ORIGIN));
    }

    @Test
    void cors_rejectsUnknownOrigin() throws Exception {
        // Act + Assert
        mockMvc.perform(options("/api/topics")
                        .header(HttpHeaders.ORIGIN, "http://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
