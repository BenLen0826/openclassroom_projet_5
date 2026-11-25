package com.openclassrooms.mddapi.config;

import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Insère les thèmes de référence au démarrage de l'API.
 * <p>
 * L'application n'a pas d'écran de création de thème : sans ces données,
 * personne ne pourrait s'abonner ni publier. L'insertion est idempotente :
 * seuls les thèmes absents (repérés par leur nom) sont ajoutés, on peut donc
 * redémarrer l'API autant de fois que nécessaire sans créer de doublon.
 * <p>
 * Données de <strong>référence</strong>, actives dans tous les environnements (conditions
 * réelles, développement, tests). Les données de <strong>démonstration</strong> (comptes,
 * articles, commentaires) sont à part, dans {@link DevDataInitializer}, actif seulement
 * avec le profil {@code dev}.
 */
@Slf4j
@Component
@Order(1) // avant DevDataInitializer (profil dev), qui utilise ces thèmes
@RequiredArgsConstructor
public class TopicDataInitializer implements ApplicationRunner {

    /** Données d'un thème de référence (valeurs immuables, distinctes de l'entité JPA). */
    record TopicSeed(String name, String description) {
    }

    static final List<TopicSeed> REFERENCE_TOPICS = List.of(
            new TopicSeed("Java", "Le langage Java et son écosystème : JVM, bibliothèques, bonnes pratiques."),
            new TopicSeed("JavaScript", "Le langage du web, côté navigateur comme côté serveur avec Node.js."),
            new TopicSeed("TypeScript", "JavaScript typé : types, interfaces, génériques et outillage."),
            new TopicSeed("Python", "Un langage polyvalent : scripts, web, data science et automatisation."),
            new TopicSeed("Angular", "Le framework front-end de Google : composants, signals, routing, formulaires."),
            new TopicSeed("Spring", "Spring Framework et Spring Boot pour construire des applications Java."),
            new TopicSeed("Bases de données", "SQL, NoSQL, modélisation et optimisation des requêtes."),
            new TopicSeed("DevOps", "Intégration et déploiement continus, conteneurs, supervision.")
    );

    private final TopicRepository topicRepository;

    /** Ajoute au démarrage les thèmes de référence absents (repérés par leur nom). */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> existingNames = topicRepository.findAll().stream()
                .map(Topic::getName)
                .collect(Collectors.toSet());

        List<Topic> missingTopics = REFERENCE_TOPICS.stream()
                .filter(seed -> !existingNames.contains(seed.name()))
                .map(seed -> new Topic(seed.name(), seed.description()))
                .toList();

        if (!missingTopics.isEmpty()) {
            topicRepository.saveAll(missingTopics);
        }
        log.info("Thèmes de référence : {} ajouté(s), {} déjà présent(s)",
                missingTopics.size(), REFERENCE_TOPICS.size() - missingTopics.size());
    }
}
