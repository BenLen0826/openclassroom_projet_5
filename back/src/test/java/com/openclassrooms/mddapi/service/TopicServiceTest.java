package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.mapper.TopicMapper;
import com.openclassrooms.mddapi.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TopicMapper topicMapper;

    @InjectMocks
    private TopicService topicService;

    @Test
    void getAllTopics_returnsMappedTopicsSortedByName() {
        // Arrange
        List<Topic> topics = List.of(new Topic("Angular", "Front-end"), new Topic("Java", "Back-end"));
        List<TopicDto> expected = List.of(
                new TopicDto(1L, "Angular", "Front-end"),
                new TopicDto(2L, "Java", "Back-end"));
        when(topicRepository.findAll(Sort.by("name"))).thenReturn(topics);
        when(topicMapper.toDtoList(topics)).thenReturn(expected);

        // Act
        List<TopicDto> result = topicService.getAllTopics();

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAllTopics_returnsEmptyListWhenNoTopic() {
        // Arrange
        when(topicRepository.findAll(Sort.by("name"))).thenReturn(List.of());
        when(topicMapper.toDtoList(List.of())).thenReturn(List.of());

        // Act
        List<TopicDto> result = topicService.getAllTopics();

        // Assert
        assertThat(result).isEmpty();
    }
}
