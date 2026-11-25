// Thèmes : s'abonner. Prérequis : l'API de test et le front tournent (voir le README).
//
// Nouveautés :
// - cy.connecterUtilisateur() : le test commence déjà connecté (compte créé par l'API) ;
// - /^Java$/ : expression régulière qui compare le nom en entier (le thème « JavaScript » existe aussi) ;
// - .parents() et .within() : agir seulement dans la carte du thème voulu.
describe('Thèmes', () => {
  it("s'abonner à un thème : le bouton devient « Déjà abonné », même après rechargement", () => {
    cy.connecterUtilisateur();
    cy.contains('.header__nav a', 'Thèmes').click();

    cy.contains('app-topic-card h2', /^Java$/)
      .parents('app-topic-card')
      .within(() => {
        cy.contains('button', "S'abonner").click();
        cy.contains('button', 'Déjà abonné').should('be.disabled');
      });

    // Rechargement : l'abonnement est relu depuis l'API, il est donc bien enregistré en base
    cy.reload();
    cy.contains('app-topic-card h2', /^Java$/)
      .parents('app-topic-card')
      .should('contain', 'Déjà abonné');
  });
});
