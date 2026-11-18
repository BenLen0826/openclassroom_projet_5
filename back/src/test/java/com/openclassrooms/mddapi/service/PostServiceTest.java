package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.CreatePostRequest;
import com.openclassrooms.mddapi.dto.PostDto;
import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.mapper.PostMapper;
import com.openclassrooms.mddapi.mapper.PostMapperImpl;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long TOPIC_ID = 3L;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TopicRepository topicRepository;

    /** Vrai mapper généré par MapStruct : on teste aussi la conversion. */
    @Spy
    private PostMapper postMapper = new PostMapperImpl();

    @InjectMocks
    private PostService postService;

    private User bob;
    private Topic java;

    @BeforeEach
    void setUp() {
        bob = new User("bob", "bob@mail.fr", "hash");
        ReflectionTestUtils.setField(bob, "id", USER_ID);
        java = new Topic("Java", "Back-end");
        ReflectionTestUtils.setField(java, "id", TOPIC_ID);
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(bob));
    }

    @Test
    void getFeed_unknownUser_throwsUnknownUser() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> postService.getFeed(USER_ID)).isInstanceOf(UnknownUserException.class);
        verify(postRepository, never()).findFeed(any());
    }

    @Test
    void create_setsAuthorFromTokenUserAndTopic() {
        // Arrange
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(java));
        when(postRepository.save(any(Post.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        PostDto result = postService.create(USER_ID, new CreatePostRequest(TOPIC_ID, "Titre", "Contenu"));

        // Assert
        ArgumentCaptor<Post> saved = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(saved.capture());
        assertThat(saved.getValue().getAuthor()).isSameAs(bob);
        assertThat(saved.getValue().getTopic()).isSameAs(java);
        assertThat(result.authorUsername()).isEqualTo("bob");
        assertThat(result.topicName()).isEqualTo("Java");
        assertThat(result.title()).isEqualTo("Titre");
    }

    @Test
    void create_unknownTopic_throwsTopicNotFound() {
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create(USER_ID, new CreatePostRequest(TOPIC_ID, "Titre", "Contenu")))
                .isInstanceOf(TopicNotFoundException.class);
        verify(postRepository, never()).save(any());
    }

    @Test
    void create_unknownUser_throwsUnknownUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create(USER_ID, new CreatePostRequest(TOPIC_ID, "Titre", "Contenu")))
                .isInstanceOf(UnknownUserException.class);
    }

    @Test
    void getById_returnsDto() {
        // Arrange
        Post post = new Post("Titre", "Contenu", bob, java);
        ReflectionTestUtils.setField(post, "id", 42L);
        Instant createdAt = Instant.parse("2026-11-18T10:00:00Z");
        ReflectionTestUtils.setField(post, "createdAt", createdAt);
        when(postRepository.findById(42L)).thenReturn(Optional.of(post));

        // Act + Assert
        assertThat(postService.getById(42L))
                .isEqualTo(new PostDto(42L, "Titre", "Contenu", createdAt, "bob", TOPIC_ID, "Java"));
    }

    @Test
    void getById_unknown_throwsPostNotFound() {
        when(postRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getById(42L)).isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void createPostRequest_stripsTitle() {
        assertThat(new CreatePostRequest(TOPIC_ID, "  Titre  ", "Contenu").title()).isEqualTo("Titre");
    }
}
