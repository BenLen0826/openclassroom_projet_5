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
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Articles : fil d'actualité, création, consultation. */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final PostMapper postMapper;

    /**
     * Fil d'actualité : articles des thèmes suivis, du plus récent au plus ancien.
     *
     * @throws UnknownUserException si le compte du token n'existe plus
     */
    @Transactional(readOnly = true)
    public List<PostDto> getFeed(Long userId) {
        UserLookup.requireExistingUser(userRepository, userId);
        return postMapper.toDtoList(postRepository.findFeed(userId));
    }

    /**
     * Publie un article. L'auteur est l'utilisateur du token, la date est remplie par
     * l'audit : rien ne vient de la requête à part le thème, le titre et le contenu.
     *
     * @throws TopicNotFoundException si le thème n'existe pas
     */
    @Transactional
    public PostDto create(Long userId, CreatePostRequest request) {
        User author = UserLookup.existingUser(userRepository, userId);
        Topic topic = topicRepository.findById(request.topicId()).orElseThrow(TopicNotFoundException::new);
        return postMapper.toDto(postRepository.save(new Post(request.title(), request.content(), author, topic)));
    }

    /**
     * Un article, lisible par tout utilisateur connecté (même s'il ne suit pas son
     * thème : l'auteur doit pouvoir ouvrir l'article qu'il vient de créer).
     */
    @Transactional(readOnly = true)
    public PostDto getById(Long postId) {
        return postMapper.toDto(postRepository.findById(postId).orElseThrow(PostNotFoundException::new));
    }
}
