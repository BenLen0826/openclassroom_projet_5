package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.DuplicateSubscriptionException;
import com.openclassrooms.mddapi.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abonnements de l'utilisateur connecté aux thèmes.
 * <p>
 * Les deux opérations sont idempotentes : s'abonner deux fois ou se désabonner d'un
 * thème non suivi ne change rien et ne lève pas d'erreur (double clic, deux onglets).
 * Les thèmes sont comparés par id ({@code Topic} n'a pas d'{@code equals}).
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;

    @Transactional
    public void subscribe(Long userId, Long topicId) {
        User user = findUser(userId);
        Topic topic = findTopic(topicId);
        boolean alreadySubscribed = user.getSubscriptions().stream()
                .anyMatch(subscribed -> subscribed.getId().equals(topicId));
        if (!alreadySubscribed) {
            user.getSubscriptions().add(topic);
            try {
                // flush : l'insertion a lieu ici, donc un doublon simultané est détecté
                // dans ce bloc et non au moment du commit (même principe qu'à l'inscription)
                userRepository.flush();
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateSubscriptionException();
            }
        }
    }

    @Transactional
    public void unsubscribe(Long userId, Long topicId) {
        User user = findUser(userId);
        findTopic(topicId); // 404 si le thème n'existe pas
        user.getSubscriptions().removeIf(subscribed -> subscribed.getId().equals(topicId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(UnknownUserException::new);
    }

    private Topic findTopic(Long topicId) {
        return topicRepository.findById(topicId).orElseThrow(TopicNotFoundException::new);
    }
}
