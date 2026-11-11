// Test de fumée : vérifie que l'application démarre et affiche la page d'accueil.
// Le parcours complet (inscription → abonnement → article → commentaire) sera ajouté à l'étape 7.
describe('Home page', () => {
  it('displays the logo and the start button', () => {
    cy.visit('/');

    cy.get('img[alt="logo"]').should('be.visible');
    cy.contains('button', 'Commencer').should('be.visible');
  });
});
