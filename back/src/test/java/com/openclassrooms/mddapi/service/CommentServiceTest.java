package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.entity.Comment;
import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.mapper.CommentMapper;
import com.openclassrooms.mddapi.mapper.CommentMapperImpl;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long POST_ID = 42L;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    /** Vrai mapper généré par MapStruct : on teste aussi la conversion. */
    @Spy
    private CommentMapper commentMapper = new CommentMapperImpl();

    @InjectMocks
    private CommentService commentService;

    private User bob;
    private Post post;

    @BeforeEach
    void setUp() {
        bob = new User("bob", "bob@mail.fr", "hash");
        ReflectionTestUtils.setField(bob, "id", USER_ID);
        post = new Post("Article", "Contenu", bob, new Topic("Java", "Back-end"));
        ReflectionTestUtils.setField(post, "id", POST_ID);
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(bob));
    }

    @Test
    void getForPost_unknownPost_throwsPostNotFound() {
        when(postRepository.existsById(POST_ID)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getForPost(POST_ID)).isInstanceOf(PostNotFoundException.class);
        verify(commentRepository, never()).findByPostIdWithAuthor(any());
    }

    @Test
    void getForPost_returnsDtos() {
        // Arrange
        Comment comment = new Comment("Bravo", bob, post);
        ReflectionTestUtils.setField(comment, "id", 1L);
        when(postRepository.existsById(POST_ID)).thenReturn(true);
        when(commentRepository.findByPostIdWithAuthor(POST_ID)).thenReturn(List.of(comment));

        // Act
        List<CommentDto> result = commentService.getForPost(POST_ID);

        // Assert
        assertThat(result).containsExactly(new CommentDto(1L, "Bravo", null, "bob"));
    }

    @Test
    void create_setsAuthorAndPost() {
        // Arrange
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        CommentDto result = commentService.create(USER_ID, POST_ID, new CreateCommentRequest("Bravo"));

        // Assert
        ArgumentCaptor<Comment> saved = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(saved.capture());
        assertThat(saved.getValue().getAuthor()).isSameAs(bob);
        assertThat(saved.getValue().getPost()).isSameAs(post);
        assertThat(result.authorUsername()).isEqualTo("bob");
        assertThat(result.content()).isEqualTo("Bravo");
    }

    @Test
    void create_unknownPost_throwsPostNotFound() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(USER_ID, POST_ID, new CreateCommentRequest("Bravo")))
                .isInstanceOf(PostNotFoundException.class);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_unknownUser_throwsUnknownUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(USER_ID, POST_ID, new CreateCommentRequest("Bravo")))
                .isInstanceOf(UnknownUserException.class);
    }
}
