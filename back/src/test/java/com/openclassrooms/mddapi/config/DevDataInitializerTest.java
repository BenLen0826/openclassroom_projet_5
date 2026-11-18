package com.openclassrooms.mddapi.config;

import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Données de démonstration du profil {@code dev}. Base H2 dédiée : ce contexte ne doit
 * pas recréer le schéma de la base partagée par les autres tests d'intégration.
 */
@SpringBootTest
@ActiveProfiles({"test", "dev"})
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:mdd-dev;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
class DevDataInitializerTest {

    @Autowired
    private DevDataInitializer devDataInitializer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsDemoAccountsWithTheDocumentedPassword() {
        User alice = userRepository.findByEmail("alice@example.com").orElseThrow();
        User bob = userRepository.findByEmail("bob@example.com").orElseThrow();

        assertThat(alice.getUsername()).isEqualTo("alice");
        assertThat(bob.getUsername()).isEqualTo("bob");
        assertThat(passwordEncoder.matches(DevDataInitializer.DEMO_PASSWORD, alice.getPassword())).isTrue();
        assertThat(DevDataInitializer.DEMO_PASSWORD).isEqualTo("Demo1234!");
    }

    @Test
    @Transactional
    void createsSubscriptionsPostsAndComments() {
        User alice = userRepository.findByEmail("alice@example.com").orElseThrow();

        assertThat(userRepository.findSubscribedTopicIds(alice.getId())).hasSize(2);
        assertThat(postRepository.count()).isEqualTo(3);
        assertThat(commentRepository.count()).isEqualTo(2);
        // Le fil d'alice n'est pas vide : ses abonnements correspondent à des articles
        assertThat(postRepository.findFeed(alice.getId())).isNotEmpty();
    }

    @Test
    void isIdempotent() {
        // Act : nouveau démarrage de l'API
        devDataInitializer.run(null);

        // Assert : aucun doublon
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(postRepository.count()).isEqualTo(3);
        assertThat(commentRepository.count()).isEqualTo(2);
    }
}
