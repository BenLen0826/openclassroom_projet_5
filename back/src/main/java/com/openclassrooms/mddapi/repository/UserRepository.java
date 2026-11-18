package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    /** L'e-mail est enregistré en minuscules : la comparaison exacte suffit. */
    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    /** Ids des thèmes suivis, en une requête (sans charger les thèmes eux-mêmes). */
    @Query("select t.id from User u join u.subscriptions t where u.id = :userId")
    Set<Long> findSubscribedTopicIds(@Param("userId") Long userId);
}
