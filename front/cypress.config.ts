import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    // Adresse du front (npm start) : cy.visit('/posts') ouvre http://localhost:4200/posts
    baseUrl: 'http://localhost:4200',
    // Adresse de l'API de test (./mvnw spring-boot:test-run), utilisée par les commandes de
    // cypress/support/commands.ts pour préparer les données sans passer par les formulaires
    expose: {
      apiUrl: 'http://localhost:8080/api',
    },
  },
});
