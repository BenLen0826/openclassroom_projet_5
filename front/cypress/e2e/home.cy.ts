// Test de fumée : l'application démarre et la page d'accueil mène à l'inscription.
// Il ne demande pas l'API : seul le front (npm start) doit tourner.
//
// Rappel des bases de Cypress :
// - cy.visit('/')            ouvre une page (baseUrl de cypress.config.ts + '/') ;
// - cy.get(sélecteur CSS)     cherche un élément ; cy.contains('a', 'texte') cherche une balise par son texte ;
// - .should(...)              vérifie, en réessayant jusqu'à 4 s (pas besoin d'attendre soi-même) ;
// - .click(), .type('texte')  agissent comme l'utilisateur.
describe("Page d'accueil", () => {
  it("propose de se connecter et de s'inscrire", () => {
    cy.visit('/');

    cy.get('img[alt="Logo MDD"]').should('be.visible');
    cy.contains('a', 'Se connecter').should('be.visible');
    cy.contains('a', "S'inscrire").click();

    cy.url().should('include', '/register');
  });
});
