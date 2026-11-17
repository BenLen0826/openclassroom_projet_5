package com.openclassrooms.mddapi.config;

import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicDataInitializerTest {

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private TopicDataInitializer initializer;

    @Captor
    private ArgumentCaptor<List<Topic>> savedTopics;

    @Test
    void run_insertsAllReferenceTopicsWhenDatabaseIsEmpty() {
        // Arrange
        when(topicRepository.findAll()).thenReturn(List.of());

        // Act
        initializer.run(null);

        // Assert
        verify(topicRepository).saveAll(savedTopics.capture());
        assertThat(savedTopics.getValue())
                .extracting(Topic::getName)
                .containsExactlyElementsOf(TopicDataInitializer.REFERENCE_TOPICS.stream()
                        .map(TopicDataInitializer.TopicSeed::name)
                        .toList());
    }

    @Test
    void run_insertsOnlyMissingTopics() {
        // Arrange
        when(topicRepository.findAll()).thenReturn(List.of(
                new Topic("Java", "déjà en base"),
                new Topic("Angular", "déjà en base")));

        // Act
        initializer.run(null);

        // Assert
        verify(topicRepository).saveAll(savedTopics.capture());
        assertThat(savedTopics.getValue())
                .extracting(Topic::getName)
                .hasSize(TopicDataInitializer.REFERENCE_TOPICS.size() - 2)
                .doesNotContain("Java", "Angular");
    }

    @Test
    void run_insertsNothingWhenAllTopicsAlreadyExist() {
        // Arrange
        List<Topic> allTopics = TopicDataInitializer.REFERENCE_TOPICS.stream()
                .map(seed -> new Topic(seed.name(), seed.description()))
                .toList();
        when(topicRepository.findAll()).thenReturn(allTopics);

        // Act
        initializer.run(null);

        // Assert
        verify(topicRepository, never()).saveAll(any());
    }
}
