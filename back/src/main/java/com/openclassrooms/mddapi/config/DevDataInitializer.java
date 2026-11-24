package com.openclassrooms.mddapi.config;

import com.openclassrooms.mddapi.entity.Comment;
import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Données de démonstration (comptes, abonnements, articles, commentaires), pour essayer
 * l'application sans tout créer à la main.
 * <p>
 * <strong>À quoi il sert</strong> : aux essais manuels et aux démonstrations (soutenance,
 * base vide sur une nouvelle machine). Il répond à la consigne de l'étape 3 « peupler la base
 * avec des données de test », reportée tant que les entités n'existaient pas.
 * <strong>Il ne sert pas aux tests automatiques</strong> : chaque test JUnit crée puis
 * supprime ses propres données, et le profil {@code test} n'active pas cette classe.
 * <p>
 * <strong>Actif uniquement avec le profil {@code dev}</strong>
 * ({@code ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"}) : ces comptes ont un mot
 * de passe connu, documenté dans le README, et ne doivent jamais exister en conditions réelles.
 * C'est pourquoi cette classe est séparée de {@link TopicDataInitializer} : les thèmes sont
 * nécessaires partout, la démonstration seulement en développement, et c'est Spring
 * ({@code @Profile}) qui garantit la séparation, pas une condition dans le code.
 * <p>
 * Idempotent : si le compte de démonstration existe déjà, rien n'est ajouté. Exécuté après
 * {@link TopicDataInitializer} ({@code @Order}), dont il utilise les thèmes.
 */
@Slf4j
@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner {

    /** Mot de passe des comptes de démonstration (respecte la règle des spécifications). */
    public static final String DEMO_PASSWORD = "Demo1234!";

    private static final String USER1_EMAIL = "user1@example.com";

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(USER1_EMAIL)) {
            log.info("Données de démonstration (profil dev) : déjà présentes, rien n'est ajouté");
            return;
        }
        Map<String, Topic> topics = topicRepository.findAll().stream()
                .collect(Collectors.toMap(Topic::getName, Function.identity()));

        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        User user1 = new User("user1", USER1_EMAIL, hash);
        user1.getSubscriptions().add(topics.get("Java"));
        user1.getSubscriptions().add(topics.get("Angular"));
        User user2 = new User("user2", "user2@example.com", hash);
        user2.getSubscriptions().add(topics.get("Java"));
        user2.getSubscriptions().add(topics.get("Spring"));
        userRepository.save(user1);
        userRepository.save(user2);

        Post records = postRepository.save(new Post("Les records en Java",
                "Depuis Java 16, les records simplifient les objets immuables : constructeur, "
                        + "accesseurs, equals, hashCode et toString sont générés.", user1, topics.get("Java")));
        postRepository.save(new Post("Les signals dans Angular",
                "Les signals gardent un état réactif et ne mettent à jour que ce qui en dépend, "
                        + "sans zone.js.", user1, topics.get("Angular")));
        Post security = postRepository.save(new Post("Sécuriser une API avec Spring Security",
                "Un JWT vérifié par OAuth2 Resource Server suffit pour une API sans session.",
                user2, topics.get("Spring")));

        commentRepository.save(new Comment("Très clair, merci pour l'exemple !", user2, records));
        commentRepository.save(new Comment("Et pour les tests, tu utilises MockMvc ?", user1, security));

        // Jamais d'e-mail ni de mot de passe dans les logs
        log.info("Données de démonstration (profil dev) : 2 comptes, 3 articles, 2 commentaires ajoutés");
    }
}
