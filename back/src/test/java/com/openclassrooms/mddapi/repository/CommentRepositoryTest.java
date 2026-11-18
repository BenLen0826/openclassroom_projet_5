package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.Comment;
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
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Test
    void findByPostIdWithAuthor_returnsOnlyThatPost_oldestFirst() {
        // Arrange : deux articles, des commentaires sur chacun, enregistrés à la suite
        Topic java = topicRepository.save(new Topic("Java-test", "Back-end"));
        User bob = userRepository.save(new User("bob", "bob@mail.fr", "hash"));
        Post post = postRepository.save(new Post("Article", "Contenu", bob, java));
        Post other = postRepository.save(new Post("Autre", "Contenu", bob, java));
        Comment first = commentRepository.saveAndFlush(new Comment("Premier", bob, post));
        commentRepository.saveAndFlush(new Comment("Ailleurs", bob, other));
        Comment second = commentRepository.saveAndFlush(new Comment("Second", bob, post));

        // Act
        List<Comment> comments = commentRepository.findByPostIdWithAuthor(post.getId());

        // Assert : seulement cet article, du plus ancien au plus récent (id en cas d'égalité)
        assertThat(comments).extracting(Comment::getId).containsExactly(first.getId(), second.getId());
        assertThat(comments.getFirst().getCreatedAt()).isNotNull();
    }
}
