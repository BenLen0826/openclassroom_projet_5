// Authentification : inscription, connexion (e-mail ou nom d'utilisateur), erreur, session
// persistante, déconnexion. Prérequis : l'API de test et le front tournent (voir le README).
//
// Nouveautés par rapport au test de fumée (home.cy.ts) :
// - cy.creerUtilisateur() : commande personnalisée (cypress/support/commands.ts) qui crée un compte
//   en appelant l'API, pour tester la connexion sans repasser par le formulaire d'inscription ;
// - .then((user) => ...) : récupérer la valeur renvoyée par une commande Cypress ;
// - cy.reload() : recharger la page, pour vérifier que la connexion est gardée.
describe('Authentification', () => {
  it("inscription par le formulaire : l'utilisateur arrive sur le fil", () => {
    const id = Date.now();
    cy.visit('/register');
    cy.get('input[name="username"]').type(`cyinscription${id}`);
    cy.get('input[name="email"]').type(`cyinscription${id}@mail.fr`);
    cy.get('input[name="password"]').type('Cypress1!');
    cy.contains('button', "S'inscrire").click();

    cy.url().should('include', '/posts');
  });

  it("connexion par e-mail, puis par nom d'utilisateur", () => {
    cy.creerUtilisateur().then((user) => {
      cy.visit('/login');
      cy.get('input[name="identifier"]').type(user.email);
      cy.get('input[name="password"]').type(user.password);
      cy.contains('button', 'Se connecter').click();
      cy.url().should('include', '/posts');

      cy.contains('.header__nav button', 'Se déconnecter').click();
      cy.visit('/login');
      cy.get('input[name="identifier"]').type(user.username);
      cy.get('input[name="password"]').type(user.password);
      cy.contains('button', 'Se connecter').click();
      cy.url().should('include', '/posts');
    });
  });

  it('refuse un mauvais mot de passe avec un message', () => {
    cy.creerUtilisateur().then((user) => {
      cy.visit('/login');
      cy.get('input[name="identifier"]').type(user.email);
      cy.get('input[name="password"]').type('Mauvais1!');
      cy.contains('button', 'Se connecter').click();

      cy.contains('Identifiant ou mot de passe incorrect.').should('be.visible');
      cy.location('pathname').should('eq', '/login');
    });
  });

  it('garde la connexion après un rechargement de la page (session persistante)', () => {
    cy.connecterUtilisateur();
    cy.reload();

    cy.location('pathname').should('eq', '/posts');
    cy.contains('.header__nav button', 'Se déconnecter').should('be.visible');
  });

  it("déconnexion : retour à l'accueil, les pages privées renvoient à la connexion", () => {
    cy.connecterUtilisateur();
    cy.contains('.header__nav button', 'Se déconnecter').click();
    cy.location('pathname').should('eq', '/');

    cy.visit('/posts');
    cy.location('pathname').should('eq', '/login');
  });
});
