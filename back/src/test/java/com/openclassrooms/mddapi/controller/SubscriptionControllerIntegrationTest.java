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
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Abonnement et désabonnement de bout en bout : sécurité, contrôleur, service, base H2. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubscriptionControllerIntegrationTest {

    private static final String URL = "/api/users/me/subscriptions/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private JwtService jwtService;

    private String bobToken;
    private Long javaId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        bobToken = tokenOf("bob");
        javaId = topicRepository.findAll().stream()
                .filter(topic -> topic.getName().equals("Java"))
                .map(Topic::getId)
                .findFirst()
                .orElseThrow();
    }

    private String tokenOf(String username) {
        return jwtService.generateToken(userRepository.save(new User(username, username + "@mail.fr", "hash")));
    }

    private ResultActions subscribe(String token, Object topicId) throws Exception {
        return mockMvc.perform(post(URL + topicId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private ResultActions subscribedTopicNames(String token) throws Exception {
        return mockMvc.perform(get("/api/topics").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    @Test
    void subscribe_returns204AndTopicIsSubscribed() throws Exception {
        subscribe(bobToken, javaId).andExpect(status().isNoContent());

        subscribedTopicNames(bobToken).andExpect(jsonPath("$[?(@.subscribed == true)].name", contains("Java")));
    }

    @Test
    void subscribeTwice_returns204BothAndKeepsOneRow() throws Exception {
        subscribe(bobToken, javaId).andExpect(status().isNoContent());
        subscribe(bobToken, javaId).andExpect(status().isNoContent());

        Long bobId = userRepository.findByUsernameIgnoreCase("bob").orElseThrow().getId();
        assertThat(userRepository.findSubscribedTopicIds(bobId)).containsExactly(javaId);
    }

    @Test
    void unsubscribe_returns204AndTopicIsNoLongerSubscribed() throws Exception {
        // Arrange
        subscribe(bobToken, javaId).andExpect(status().isNoContent());

        // Act
        mockMvc.perform(delete(URL + javaId).header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isNoContent());

        // Assert
        subscribedTopicNames(bobToken).andExpect(jsonPath("$[?(@.subscribed == true)]", empty()));
    }

    @Test
    void unsubscribe_notSubscribed_returns204() throws Exception {
        mockMvc.perform(delete(URL + javaId).header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void subscribe_unknownTopic_returns404() throws Exception {
        subscribe(bobToken, 999_999)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Thème introuvable."));
    }

    @Test
    void subscribe_nonNumericTopicId_returns400() throws Exception {
        subscribe(bobToken, "abc").andExpect(status().isBadRequest());
    }

    @Test
    void subscribe_withoutToken_returns401() throws Exception {
        mockMvc.perform(post(URL + javaId)).andExpect(status().isUnauthorized());
    }

    @Test
    void subscriptionsAreIsolatedBetweenUsers() throws Exception {
        // Arrange
        String aliceToken = tokenOf("alice");

        // Act
        subscribe(bobToken, javaId).andExpect(status().isNoContent());

        // Assert
        subscribedTopicNames(aliceToken).andExpect(jsonPath("$[?(@.subscribed == true)]", empty()));
    }
}
