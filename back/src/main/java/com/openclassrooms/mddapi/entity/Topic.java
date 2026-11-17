package com.openclassrooms.mddapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Thème de programmation auquel un utilisateur peut s'abonner.
 * <p>
 * Les thèmes sont des données de référence : ils sont insérés au démarrage
 * (voir {@code TopicDataInitializer}) et ne sont jamais créés ni modifiés
 * depuis l'API, d'où l'absence de setters.
 */
@Entity
@Table(name = "topics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requis par JPA, inutile ailleurs
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    public Topic(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
