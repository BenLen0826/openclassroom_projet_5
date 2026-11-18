package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Test
    void save_fillsAuditDates() {
        // Act
        User saved = userRepository.saveAndFlush(new User("bob", "bob@mail.fr", "hash"));

        // Assert
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void existsByUsernameIgnoreCase_matchesOtherCase() {
        // Arrange
        userRepository.saveAndFlush(new User("bob", "bob@mail.fr", "hash"));

        // Act + Assert
        assertThat(userRepository.existsByUsernameIgnoreCase("BOB")).isTrue();
        assertThat(userRepository.existsByUsernameIgnoreCase("alice")).isFalse();
    }

    @Test
    void duplicateEmail_isNeverWrittenToTheLogs(CapturedOutput output) {
        // Arrange : cas de deux inscriptions identiques simultanées
        userRepository.saveAndFlush(new User("bob", "doublon@mail.fr", "hash"));

        // Act
        assertThatThrownBy(() -> userRepository.saveAndFlush(new User("bobby", "doublon@mail.fr", "hash")))
                .isInstanceOf(DataIntegrityViolationException.class);

        // Assert : le message SQL brut (qui contient la valeur en double) n'est pas journalisé
        assertThat(output.getAll()).doesNotContain("doublon@mail.fr");
    }

    @Test
    void findByUsernameIgnoreCase_findsOtherCase() {
        // Arrange
        User bob = userRepository.saveAndFlush(new User("bob", "bob@mail.fr", "hash"));

        // Act + Assert
        assertThat(userRepository.findByUsernameIgnoreCase("BOB")).contains(bob);
    }

    @Test
    void findByEmail_returnsEmptyForUnknownEmail() {
        // Arrange
        userRepository.saveAndFlush(new User("bob", "bob@mail.fr", "hash"));

        // Act + Assert
        assertThat(userRepository.findByEmail("alice@mail.fr")).isEmpty();
        assertThat(userRepository.findByEmail("bob@mail.fr")).isPresent();
    }

    @Test
    void findSubscribedTopicIds_returnsOnlyThatUsersTopics() {
        // Arrange : deux utilisateurs, abonnements différents
        Topic java = topicRepository.save(new Topic("Java-test", "Back-end"));
        Topic angular = topicRepository.save(new Topic("Angular-test", "Front-end"));
        User bob = new User("bob", "bob@mail.fr", "hash");
        bob.getSubscriptions().add(java);
        User alice = new User("alice", "alice@mail.fr", "hash");
        alice.getSubscriptions().add(angular);
        userRepository.saveAndFlush(bob);
        userRepository.saveAndFlush(alice);

        // Act + Assert
        assertThat(userRepository.findSubscribedTopicIds(bob.getId())).containsExactly(java.getId());
        assertThat(userRepository.findSubscribedTopicIds(alice.getId())).containsExactly(angular.getId());
    }

    @Test
    void existsByEmail_matchesExactEmail() {
        // Arrange
        userRepository.saveAndFlush(new User("bob", "bob@mail.fr", "hash"));

        // Act + Assert
        assertThat(userRepository.existsByEmail("bob@mail.fr")).isTrue();
        assertThat(userRepository.existsByEmail("alice@mail.fr")).isFalse();
    }
}
