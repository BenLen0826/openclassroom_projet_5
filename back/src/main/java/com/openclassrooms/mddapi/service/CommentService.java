package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.entity.Comment;
import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.mapper.CommentMapper;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Commentaires d'un article : lecture et ajout, par tout utilisateur connecté. */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    /**
     * Commentaires d'un article, dans l'ordre chronologique.
     *
     * @throws PostNotFoundException si l'article n'existe pas (une liste vide masquerait
     *                               une mauvaise adresse)
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getForPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException();
        }
        return commentMapper.toDtoList(commentRepository.findByPostIdWithAuthor(postId));
    }

    /**
     * Ajoute un commentaire. L'auteur est l'utilisateur du token, la date est remplie
     * par l'audit.
     *
     * @throws PostNotFoundException si l'article n'existe pas
     * @throws UnknownUserException  si le compte du token n'existe plus
     */
    @Transactional
    public CommentDto create(Long userId, Long postId, CreateCommentRequest request) {
        User author = UserLookup.existingUser(userRepository, userId);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        return commentMapper.toDto(commentRepository.save(new Comment(request.content(), author, post)));
    }
}
