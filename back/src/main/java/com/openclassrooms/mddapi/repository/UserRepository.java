package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** L'e-mail est enregistré en minuscules : la comparaison exacte suffit. */
    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);
}
