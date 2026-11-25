// Commentaires : commenter un article. Prérequis : l'API de test et le front tournent.
//
// Nouveautés :
// - cy.creerArticle() renvoie l'article créé : on utilise son id pour ouvrir directement sa page ;
// - .should('contain', ...) sur l'élément trouvé : vérifier deux textes dans le même commentaire.
describe('Commentaires', () => {
  it("commenter un article : le commentaire s'affiche avec le nom de son auteur", () => {
    const texte = `Commentaire Cypress ${Date.now()}`;
    cy.connecterUtilisateur().then((user) => {
      cy.creerArticle(user, 'Java', `Article à commenter ${Date.now()}`).then((article) => {
        cy.visit(`/posts/${article.id}`);
      });

      cy.intercept('POST', '**/api/posts/*/comments').as('commentaire');
      cy.get('textarea[placeholder="Écrivez ici votre commentaire"]').type(texte);
      cy.get('button[aria-label="Envoyer le commentaire"]').click();
      cy.wait('@commentaire').its('response.statusCode').should('eq', 201);

      // L'auteur vient du token (défini par le serveur), jamais d'une saisie
      cy.contains('app-comment-item', texte).should('contain', user.username);
    });
  });
});
