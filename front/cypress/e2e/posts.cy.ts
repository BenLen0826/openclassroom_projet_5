// Articles : création, fil d'actualité, tri. Prérequis : l'API de test et le front tournent.
//
// Nouveautés :
// - cy.abonnerA() et cy.creerArticle() : préparer les données par l'API avant le test ;
// - cy.ouvrirEnTantQue(user, chemin) : ouvrir une page déjà connecté avec un compte préparé ;
// - cy.intercept + cy.wait : attendre la vraie réponse de l'API avant de vérifier l'écran
//   (jamais une durée fixe comme cy.wait(2000)) ;
// - .select() : choisir dans une liste déroulante ;
// - .should(callback) : écrire sa propre vérification (ici l'ordre de deux articles dans la liste).
describe('Articles', () => {
  it("créer un article : sa page s'ouvre, puis il apparaît dans le fil", () => {
    const titre = `Article Cypress ${Date.now()}`;
    cy.connecterUtilisateur().then((user) => cy.abonnerA(user, 'Java'));

    // On « surveille » la requête de création AVANT de cliquer, pour pouvoir l'attendre ensuite
    cy.intercept('POST', '**/api/posts').as('creation');
    cy.contains('a', 'Créer un article').click();
    cy.get('select[name="topicId"]').select('Java');
    cy.get('input[name="title"]').type(titre);
    cy.get('textarea[name="content"]').type('Contenu écrit par Cypress');
    cy.contains('button', 'Créer').click();
    cy.wait('@creation').its('response.statusCode').should('eq', 201);

    cy.url().should('match', /\/posts\/\d+$/); // /posts/ suivi de l'identifiant de l'article
    cy.contains('h1', titre).should('be.visible');

    // Le fil ne montre que les thèmes suivis : l'article y est, car l'utilisateur suit « Java »
    cy.contains('.header__nav a', 'Articles').click();
    cy.contains('app-post-card', titre).should('be.visible');
  });

  it('trier le fil : du plus récent au plus ancien, puis l’inverse', () => {
    const id = Date.now();
    const ancien = `Ancien ${id}`;
    const recent = `Récent ${id}`;
    cy.creerUtilisateur().then((user) => {
      cy.abonnerA(user, 'Java');
      cy.creerArticle(user, 'Java', ancien);
      cy.creerArticle(user, 'Java', recent);
      cy.ouvrirEnTantQue(user, '/posts');
    });

    // Le fil contient aussi les articles « Java » des autres tests : on ne regarde pas la première
    // carte, mais l'ordre relatif de NOS deux articles (.should(callback) réessaie jusqu'à 4 s)
    const verifierOrdre = (premier: string, second: string) =>
      cy.get('app-post-card h2').should(($titres) => {
        const titres = [...$titres].map((titre) => titre.textContent?.trim());
        expect(titres.indexOf(premier), `« ${premier} » avant « ${second} »`).to.be.lessThan(
          titres.indexOf(second),
        );
      });

    verifierOrdre(recent, ancien); // par défaut : du plus récent au plus ancien
    cy.contains('button', 'Trier par').click();
    verifierOrdre(ancien, recent); // après le clic : du plus ancien au plus récent
  });
});
