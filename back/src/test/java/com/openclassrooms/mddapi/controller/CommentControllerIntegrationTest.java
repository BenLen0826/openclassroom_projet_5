package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.repository.CommentRepository;
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

/** Commentaires de bout en bout : sécurité, validation, service, base H2. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JwtService jwtService;

    private String bobToken;
    private Long postId;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        User bob = userRepository.save(new User("bob", "bob@mail.fr", "hash"));
        bobToken = jwtService.generateToken(bob);
        Topic java = topicRepository.findAll().stream()
                .filter(topic -> topic.getName().equals("Java"))
                .findFirst()
                .orElseThrow();
        postId = postRepository.save(new Post("Article", "Contenu", bob, java)).getId();
    }

    /**
     * Les commentaires d'abord, puis les articles : les autres classes de test vident
     * {@code users} et {@code posts}, ce que des lignes restantes empêcheraient (clés étrangères).
     */
    @AfterEach
    void cleanDatabase() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String url(Object id) {
        return "/api/posts/" + id + "/comments";
    }

    private ResultActions send(Object id, String json) throws Exception {
        return mockMvc.perform(post(url(id)).header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions send(String content) throws Exception {
        return send(postId, "{\"content\": \"" + content + "\"}");
    }

    private ResultActions list(Object id) throws Exception {
        return mockMvc.perform(get(url(id)).header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken));
    }

    @Test
    void getComments_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(url(postId))).andExpect(status().isUnauthorized());
    }

    @Test
    void getComments_emptyForAPostWithoutComment() throws Exception {
        list(postId).andExpect(status().isOk()).andExpect(jsonPath("$", empty()));
    }

    @Test
    void getComments_returnsChronologicalList() throws Exception {
        // Arrange
        send("Premier").andExpect(status().isCreated());
        send("Second").andExpect(status().isCreated());

        // Act + Assert
        list(postId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].content", contains("Premier", "Second")))
                .andExpect(jsonPath("$[0].authorUsername").value("bob"));
    }

    @Test
    void getComments_unknownPost_returns404() throws Exception {
        list(999_999)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Article introuvable."));
    }

    @Test
    void create_returns201WithServerSideAuthorAndDate() throws Exception {
        // Act : le client tente d'imposer l'auteur et la date
        send(postId, """
                {"content": "Bravo", "authorUsername": "pirate", "createdAt": "2000-01-01T00:00:00Z"}
                """)
                // Assert : ignorés, le serveur met les siens
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("Bravo"))
                .andExpect(jsonPath("$.authorUsername").value("bob"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.createdAt", not("2000-01-01T00:00:00Z")));
    }

    @Test
    void create_blank_returns400() throws Exception {
        send("   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").value("Le commentaire est obligatoire."));
    }

    @Test
    void create_20000_returns201_and20001_returns400() throws Exception {
        send("x".repeat(20_000)).andExpect(status().isCreated());
        send("x".repeat(20_001))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").value("Le commentaire ne doit pas dépasser 20 000 caractères."));
    }

    @Test
    void create_unknownPost_returns404() throws Exception {
        send(999_999, "{\"content\": \"Bravo\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Article introuvable."));
    }

    @Test
    void create_nonNumericPostId_returns400() throws Exception {
        send("abc", "{\"content\": \"Bravo\"}").andExpect(status().isBadRequest());
    }
}
