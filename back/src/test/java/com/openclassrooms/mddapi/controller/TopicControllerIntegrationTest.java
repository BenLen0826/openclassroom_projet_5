package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    void getAllTopics_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/topics")).andExpect(status().isUnauthorized());
    }

    @Test
    void getAllTopics_returnsSortedTopicsWithSubscriptionState() throws Exception {
        // Arrange : un utilisateur abonné au seul thème « Java »
        Topic java = topicRepository.findAll().stream()
                .filter(topic -> topic.getName().equals("Java"))
                .findFirst()
                .orElseThrow();
        User bob = new User("bob", "bob@mail.fr", "hash");
        bob.getSubscriptions().add(java);
        String token = jwtService.generateToken(userRepository.save(bob));

        // Act + Assert
        mockMvc.perform(get("/api/topics").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[*].name", contains(
                        "Angular", "Bases de données", "DevOps", "Java",
                        "JavaScript", "Python", "Spring", "TypeScript")))
                .andExpect(jsonPath("$[?(@.subscribed == true)].name", contains("Java")))
                .andExpect(jsonPath("$[0].description").isNotEmpty());
    }

    @Test
    void getAllTopics_withTokenOfDeletedUser_returns401() throws Exception {
        // Arrange : token encore valide, mais le compte n'existe plus
        User bob = userRepository.save(new User("bob", "bob@mail.fr", "hash"));
        String token = jwtService.generateToken(bob);
        userRepository.delete(bob);

        // Act + Assert
        mockMvc.perform(get("/api/topics").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cors_allowsFrontOrigin() throws Exception {
        // Act + Assert : requête de pré-vérification envoyée par le navigateur (sans token)
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
