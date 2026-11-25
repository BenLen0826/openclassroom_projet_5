package com.openclassrooms.mddapi;

import org.springframework.boot.SpringApplication;

/**
 * Lance l'API pour les tests end-to-end Cypress : {@code ./mvnw spring-boot:test-run}.
 * <p>
 * Le profil {@code e2e} est toujours activé ici : impossible de l'oublier dans la commande et de
 * démarrer par erreur sur la base MySQL. Ce profil ({@code src/test/resources/application-e2e.properties})
 * et la base H2 (dépendance de test) ne sont visibles qu'avec le classpath des tests : l'application
 * livrée n'en dépend pas.
 */
public class TestMddApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(MddApiApplication::main).withAdditionalProfiles("e2e").run(args);
    }
}
