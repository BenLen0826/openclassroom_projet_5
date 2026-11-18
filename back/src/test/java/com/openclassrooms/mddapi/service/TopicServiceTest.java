package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.mapper.TopicMapper;
import com.openclassrooms.mddapi.mapper.TopicMapperImpl;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    private static final Long USER_ID = 7L;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    /** Vrai mapper généré par MapStruct : on teste aussi la conversion. */
    @Spy
    private TopicMapper topicMapper = new TopicMapperImpl();

    @InjectMocks
    private TopicService topicService;

    private static Topic topic(long id, String name) {
        Topic topic = new Topic(name, name + " description");
        ReflectionTestUtils.setField(topic, "id", id);
        return topic;
    }

    @Test
    void getAllTopics_marksOnlySubscribedTopics() {
        // Arrange
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(topicRepository.findAll(Sort.by("name"))).thenReturn(List.of(topic(1, "Angular"), topic(2, "Java")));
        when(userRepository.findSubscribedTopicIds(USER_ID)).thenReturn(Set.of(2L));

        // Act
        List<TopicDto> result = topicService.getAllTopics(USER_ID);

        // Assert
        assertThat(result).containsExactly(
                new TopicDto(1L, "Angular", "Angular description", false),
                new TopicDto(2L, "Java", "Java description", true));
    }

    @Test
    void getAllTopics_returnsEmptyListWhenNoTopic() {
        // Arrange
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(topicRepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(userRepository.findSubscribedTopicIds(USER_ID)).thenReturn(Set.of());

        // Act + Assert
        assertThat(topicService.getAllTopics(USER_ID)).isEmpty();
    }

    @Test
    void getAllTopics_throwsWhenUserNoLongerExists() {
        // Arrange : token encore valide, compte supprimé
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> topicService.getAllTopics(USER_ID)).isInstanceOf(UnknownUserException.class);
        verifyNoInteractions(topicRepository);
    }
}
