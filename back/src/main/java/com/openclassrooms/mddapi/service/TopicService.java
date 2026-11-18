package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.mapper.TopicMapper;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final TopicMapper topicMapper;

    /**
     * Renvoie tous les thèmes, triés par nom, avec l'état d'abonnement de l'utilisateur.
     *
     * @throws UnknownUserException si l'utilisateur du token n'existe plus
     */
    @Transactional(readOnly = true)
    public List<TopicDto> getAllTopics(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UnknownUserException();
        }
        Set<Long> subscribedIds = userRepository.findSubscribedTopicIds(userId);
        return topicRepository.findAll(Sort.by("name")).stream()
                .map(topic -> topicMapper.toDto(topic, subscribedIds.contains(topic.getId())))
                .toList();
    }
}
