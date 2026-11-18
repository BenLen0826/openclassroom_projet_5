package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Test
    void findFeed_returnsOnlyFollowedTopics_newestFirst() {
        // Arrange : bob suit « Java-test » mais pas « Angular-test »
        Topic java = topicRepository.save(new Topic("Java-test", "Back-end"));
        Topic angular = topicRepository.save(new Topic("Angular-test", "Front-end"));
        User bob = new User("bob", "bob@mail.fr", "hash");
        bob.getSubscriptions().add(java);
        userRepository.saveAndFlush(bob);
        Post first = postRepository.saveAndFlush(new Post("Premier", "Contenu", bob, java));
        postRepository.saveAndFlush(new Post("Hors fil", "Contenu", bob, angular));
        Post second = postRepository.saveAndFlush(new Post("Second", "Contenu", bob, java));

        // Act
        List<Post> feed = postRepository.findFeed(bob.getId());

        // Assert : seulement le thème suivi, le plus récent d'abord
        assertThat(feed).extracting(Post::getId).containsExactly(second.getId(), first.getId());
    }

    @Test
    void save_fillsCreatedAt() {
        // Arrange
        Topic java = topicRepository.save(new Topic("Java-test", "Back-end"));
        User bob = userRepository.save(new User("bob", "bob@mail.fr", "hash"));

        // Act
        Post post = postRepository.saveAndFlush(new Post("Titre", "Contenu", bob, java));

        // Assert
        assertThat(post.getCreatedAt()).isNotNull();
    }
}
