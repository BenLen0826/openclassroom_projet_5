package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.DuplicateSubscriptionException;
import com.openclassrooms.mddapi.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long TOPIC_ID = 3L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("bob", "bob@mail.fr", "hash");
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    /** Nouvelle instance à chaque appel : simule deux chargements distincts du même thème. */
    private static Topic topic() {
        Topic topic = new Topic("Java", "Back-end");
        ReflectionTestUtils.setField(topic, "id", TOPIC_ID);
        return topic;
    }

    @Test
    void subscribe_addsTopic() {
        // Arrange
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic()));

        // Act
        subscriptionService.subscribe(USER_ID, TOPIC_ID);

        // Assert
        assertThat(user.getSubscriptions()).extracting(Topic::getId).containsExactly(TOPIC_ID);
    }

    @Test
    void subscribe_twice_keepsASingleSubscription() {
        // Arrange : deux objets Topic différents pour le même id (pas d'equals sur l'entité)
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic()), Optional.of(topic()));

        // Act
        subscriptionService.subscribe(USER_ID, TOPIC_ID);
        subscriptionService.subscribe(USER_ID, TOPIC_ID);

        // Assert
        assertThat(user.getSubscriptions()).hasSize(1);
    }

    @Test
    void subscribe_concurrentDuplicate_throwsDuplicateSubscription() {
        // Arrange : deux requêtes simultanées (deux onglets) ; la base refuse la seconde ligne
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic()));
        doThrow(new DataIntegrityViolationException("Duplicate entry")).when(userRepository).flush();

        // Act + Assert : exception dédiée (renvoyée en 204), pas une erreur 500
        assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, TOPIC_ID))
                .isInstanceOf(DuplicateSubscriptionException.class);
    }

    @Test
    void unsubscribe_unknownUser_throwsUnknownUser() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> subscriptionService.unsubscribe(USER_ID, TOPIC_ID))
                .isInstanceOf(UnknownUserException.class);
    }

    @Test
    void unsubscribe_removesTopicById() {
        // Arrange : le thème suivi est un autre objet que celui chargé pour le retrait
        user.getSubscriptions().add(topic());
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic()));

        // Act
        subscriptionService.unsubscribe(USER_ID, TOPIC_ID);

        // Assert
        assertThat(user.getSubscriptions()).isEmpty();
    }

    @Test
    void unsubscribe_notSubscribed_doesNothing() {
        // Arrange
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic()));

        // Act
        subscriptionService.unsubscribe(USER_ID, TOPIC_ID);

        // Assert
        assertThat(user.getSubscriptions()).isEmpty();
    }

    @Test
    void subscribe_unknownTopic_throwsTopicNotFound() {
        // Arrange
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, TOPIC_ID))
                .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    void unsubscribe_unknownTopic_throwsTopicNotFound() {
        // Arrange
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> subscriptionService.unsubscribe(USER_ID, TOPIC_ID))
                .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    void subscribe_unknownUser_throwsUnknownUser() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, TOPIC_ID))
                .isInstanceOf(UnknownUserException.class);
    }
}
