package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

/**
 * Accès aux comptes (Spring Data JPA) : recherche par e-mail ou nom d'utilisateur (sans tenir compte
 * de la casse pour le nom), vérification d'unicité à l'inscription et à la modification du profil,
 * et lecture des identifiants des thèmes suivis.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** L'e-mail est enregistré en minuscules : la comparaison exacte suffit. */
    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    /** Utilisé par la modification du profil : garder son propre e-mail n'est pas un conflit. */
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    /** Ids des thèmes suivis, en une requête (sans charger les thèmes eux-mêmes). */
    @Query("select t.id from User u join u.subscriptions t where u.id = :userId")
    Set<Long> findSubscribedTopicIds(@Param("userId") Long userId);
}
