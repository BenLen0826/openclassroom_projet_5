package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.mapper.TopicMapper;
import com.openclassrooms.mddapi.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    /**
     * Renvoie tous les thèmes, triés par nom pour un affichage stable.
     */
    @Transactional(readOnly = true)
    public List<TopicDto> getAllTopics() {
        return topicMapper.toDtoList(topicRepository.findAll(Sort.by("name")));
    }
}
