// Profil : modifier ses informations, se désabonner. Prérequis : l'API de test et le front tournent.
//
// Nouveautés :
// - .clear() puis .type() : remplacer la valeur d'un champ pré-rempli ;
// - .should('have.value', ...) : vérifier le contenu d'un champ ;
// - .should('not.exist') : vérifier qu'un élément a disparu.
describe('Profil', () => {
  it("modifier son nom d'utilisateur : le nouveau nom est enregistré", () => {
    const nouveauNom = `cyrenomme${Date.now()}`;
    cy.connecterUtilisateur();
    cy.get('.header__nav a[aria-label="Profil"]').click();

    cy.get('input[name="username"]').clear().type(nouveauNom);
    cy.contains('button', 'Sauvegarder').click();
    cy.contains('Profil mis à jour.').should('be.visible');

    // Rechargement : le nom est relu depuis l'API, il est donc bien enregistré en base
    cy.reload();
    cy.get('input[name="username"]').should('have.value', nouveauNom);
  });

  it('se désabonner : le thème quitte les abonnements et ses articles quittent le fil', () => {
    const titre = `Article Cypress ${Date.now()}`;
    cy.creerUtilisateur().then((user) => {
      cy.abonnerA(user, 'Java');
      cy.creerArticle(user, 'Java', titre);
      cy.ouvrirEnTantQue(user, '/profile');
    });

    cy.contains('app-topic-card h3', /^Java$/)
      .parents('app-topic-card')
      .within(() => cy.contains('button', 'Se désabonner').click());
    cy.contains("Vous n'êtes abonné à aucun thème.").should('be.visible');

    cy.contains('.header__nav a', 'Articles').click();
    cy.contains('Aucun article pour l').should('be.visible');
    cy.contains('app-post-card', titre).should('not.exist');
  });
});
