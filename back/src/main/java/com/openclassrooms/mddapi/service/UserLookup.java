package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.UnknownUserException;
import com.openclassrooms.mddapi.repository.UserRepository;

/**
 * Recherche de l'utilisateur connecté, commune aux services.
 * <p>
 * Le token peut rester valide alors que son compte n'existe plus : les deux méthodes répondent
 * alors {@code 401} ({@link UnknownUserException}). Méthodes statiques qui appellent le repository
 * reçu, plutôt que des méthodes {@code default} de {@link UserRepository} : les tests unitaires des
 * services, qui simulent {@code findById} et {@code existsById}, restent valables tels quels.
 */
final class UserLookup {

    private UserLookup() {
    }

    /**
     * Compte de l'utilisateur, chargé depuis la base.
     *
     * @throws UnknownUserException compte introuvable ({@code 401})
     */
    static User existingUser(UserRepository userRepository, Long userId) {
        return userRepository.findById(userId).orElseThrow(UnknownUserException::new);
    }

    /**
     * Vérifie que le compte existe, sans le charger.
     *
     * @throws UnknownUserException compte introuvable ({@code 401})
     */
    static void requireExistingUser(UserRepository userRepository, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UnknownUserException();
        }
    }
}
