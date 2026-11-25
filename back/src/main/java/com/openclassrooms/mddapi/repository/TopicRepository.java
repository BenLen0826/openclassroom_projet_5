package com.openclassrooms.mddapi.repository;

import com.openclassrooms.mddapi.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux thèmes (Spring Data JPA) : seules les méthodes héritées de {@code JpaRepository} servent. */
public interface TopicRepository extends JpaRepository<Topic, Long> {
}
