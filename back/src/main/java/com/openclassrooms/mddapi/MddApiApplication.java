package com.openclassrooms.mddapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Point d'entrée de l'API MDD (Spring Boot).
 * <p>
 * {@code @EnableJpaAuditing} active Spring Data JPA Auditing, qui remplit les dates
 * {@code @CreatedDate} et {@code @LastModifiedDate} des entités : elles ne sont jamais saisies.
 */
@EnableJpaAuditing
@SpringBootApplication
public class MddApiApplication {

    /**
     * Démarre l'API.
     *
     * @param args arguments de la ligne de commande (ex. {@code --server.port=8081})
     */
    public static void main(String[] args) {
        SpringApplication.run(MddApiApplication.class, args);
    }

}
