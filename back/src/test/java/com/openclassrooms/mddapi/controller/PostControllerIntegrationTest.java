package com.openclassrooms.mddapi.controller;

import com.jayway.jsonpath.JsonPath;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import org.junit.jupiter.api.AfterEach;
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Articles de bout en bout : sécurité, validation, service, base H2. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerIntegrationTest {

    private static final String URL = "/api/posts";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtService jwtService;

    private Topic java;
    private Topic angular;
    private String bobToken;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        java = topicNamed("Java");
        angular = topicNamed("Angular");
        bobToken = tokenOf("bob", java);
    }

    /**
     * Les autres classes de test vident la table {@code users} : des articles restés
     * en base les en empêcheraient (clé étrangère vers l'auteur).
     */
    @AfterEach
    void deletePosts() {
        postRepository.deleteAll();
    }

    private Topic topicNamed(String name) {
        return topicRepository.findAll().stream()
                .filter(topic -> topic.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    /** Crée un compte abonné aux thèmes donnés et renvoie son token. */
    private String tokenOf(String username, Topic... followed) {
        User user = new User(username, username + "@mail.fr", "hash");
        for (Topic topic : followed) {
            user.getSubscriptions().add(topic);
        }
        return jwtService.generateToken(userRepository.save(user));
    }

    private ResultActions create(String token, String json) throws Exception {
        return mockMvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions create(String token, Long topicId, String title, String content) throws Exception {
        return create(token, """
                {"topicId": %d, "title": "%s", "content": "%s"}
                """.formatted(topicId, title, content));
    }

    private ResultActions feed(String token) throws Exception {
        return mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    @Test
    void getFeed_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void getFeed_withoutSubscription_isEmpty() throws Exception {
        // Arrange : un article existe, mais alice ne suit aucun thème
        create(bobToken, java.getId(), "Article Java", "Contenu").andExpect(status().isCreated());
        String aliceToken = tokenOf("alice");

        // Act + Assert
        feed(aliceToken).andExpect(status().isOk()).andExpect(jsonPath("$", empty()));
    }

    @Test
    void getFeed_returnsFollowedTopicsNewestFirst() throws Exception {
        // Arrange
        create(bobToken, java.getId(), "Premier", "Contenu").andExpect(status().isCreated());
        create(bobToken, angular.getId(), "Hors fil", "Contenu").andExpect(status().isCreated());
        create(bobToken, java.getId(), "Second", "Contenu").andExpect(status().isCreated());

        // Act + Assert
        feed(bobToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", contains("Second", "Premier")))
                .andExpect(jsonPath("$[0].topicName").value("Java"))
                .andExpect(jsonPath("$[0].authorUsername").value("bob"));
    }

    @Test
    void feedsAreIsolatedBetweenUsers() throws Exception {
        // Arrange : alice suit Angular, bob suit Java
        String aliceToken = tokenOf("alice", angular);
        create(bobToken, java.getId(), "Article Java", "Contenu").andExpect(status().isCreated());
        create(aliceToken, angular.getId(), "Article Angular", "Contenu").andExpect(status().isCreated());

        // Act + Assert
        feed(bobToken).andExpect(jsonPath("$[*].title", contains("Article Java")));
        feed(aliceToken).andExpect(jsonPath("$[*].title", contains("Article Angular")));
    }

    @Test
    void create_returns201WithServerSideAuthorAndDate() throws Exception {
        // Act : le client tente d'imposer l'auteur et la date
        create(bobToken, """
                {"topicId": %d, "title": "Titre", "content": "Contenu",
                 "authorUsername": "pirate", "createdAt": "2000-01-01T00:00:00Z"}
                """.formatted(java.getId()))
                // Assert : ignorés, le serveur met les siens
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.authorUsername").value("bob"))
                .andExpect(jsonPath("$.createdAt", not("2000-01-01T00:00:00Z")))
                .andExpect(jsonPath("$.topicName").value("Java"));
    }

    @Test
    void create_blankFields_returns400() throws Exception {
        create(bobToken, """
                {"topicId": null, "title": " ", "content": ""}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.topicId").value("Le thème est obligatoire."))
                .andExpect(jsonPath("$.errors.title").value("Le titre est obligatoire."))
                .andExpect(jsonPath("$.errors.content").value("Le contenu est obligatoire."));
    }

    @Test
    void create_titleOf255_returns201_and256_returns400() throws Exception {
        create(bobToken, java.getId(), "a".repeat(255), "Contenu").andExpect(status().isCreated());
        create(bobToken, java.getId(), "a".repeat(256), "Contenu")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Le titre ne doit pas dépasser 255 caractères."));
    }

    @Test
    void create_longContent_returns201() throws Exception {
        create(bobToken, java.getId(), "Titre", "x".repeat(10_000))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("x".repeat(10_000)));
    }

    @Test
    void create_contentOf20000_returns201_and20001_returns400() throws Exception {
        // Limite alignée sur la colonne TEXT de MySQL (65 535 octets, même en caractères de 4 octets)
        create(bobToken, java.getId(), "Titre", "x".repeat(20_000)).andExpect(status().isCreated());
        create(bobToken, java.getId(), "Titre", "x".repeat(20_001))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").value("Le contenu ne doit pas dépasser 20 000 caractères."));
    }

    @Test
    void create_unknownTopic_returns404() throws Exception {
        create(bobToken, 999_999L, "Titre", "Contenu")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Thème introuvable."));
    }

    @Test
    void getById_returns200EvenForATopicNotFollowed() throws Exception {
        // Arrange : bob publie dans Angular, qu'il ne suit pas
        String response = create(bobToken, angular.getId(), "Titre", "Contenu")
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(response, "$.id");

        // Act + Assert
        mockMvc.perform(get(URL + "/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Titre"))
                .andExpect(jsonPath("$.topicName").value("Angular"));
    }

    @Test
    void getById_unknown_returns404() throws Exception {
        mockMvc.perform(get(URL + "/999999").header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Article introuvable."));
    }

    @Test
    void getById_nonNumeric_returns400() throws Exception {
        mockMvc.perform(get(URL + "/abc").header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
                .andExpect(status().isBadRequest());
    }
}
