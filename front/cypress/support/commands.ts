// Commandes Cypress réutilisables : elles préparent le point de départ d'un test en appelant
// directement l'API (cy.request), sans passer par les formulaires. Chaque test reste ainsi
// indépendant et commence là où il en a besoin ; les formulaires eux-mêmes sont testés une fois,
// dans le fichier de test de leur fonctionnalité (ex. l'inscription dans auth.cy.ts).

/** Utilisateur de test créé par l'API, avec son token JWT. */
export interface UtilisateurTest {
  username: string;
  email: string;
  password: string;
  token: string;
}

/** Article renvoyé par l'API. */
export interface ArticleTest {
  id: number;
  title: string;
}

/** Clé du token dans le navigateur, la même que dans le front (`AuthStore`). */
const TOKEN_STORAGE_KEY = 'mdd_token';

// Cypress.expose (Cypress 16) remplace Cypress.env, supprimé dans cette version
const apiUrl = (): string => Cypress.expose('apiUrl');

/** Identifiants uniques à chaque appel : on peut relancer les tests sans redémarrer l'API (pas de 409). */
function identifiantsUniques(): Omit<UtilisateurTest, 'token'> {
  const id = `${Date.now()}${Cypress._.random(1000, 9999)}`;
  return { username: `cy${id}`, email: `cy${id}@mail.fr`, password: 'Cypress1!' };
}

// Déclaration des types : permet à TypeScript de connaître les nouvelles commandes (autocomplétion).
// « namespace » est imposé par Cypress pour étendre son interface Chainable.
declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      /** Crée un compte (sans passer par le formulaire) et le renvoie avec son token. */
      creerUtilisateur(): Chainable<UtilisateurTest>;
      /** Ouvre cette page de l'application en étant déjà connecté avec ce compte. */
      ouvrirEnTantQue(user: UtilisateurTest, chemin: string): Chainable<void>;
      /** Crée un compte, puis ouvre l'application déjà connecté (arrivée sur le fil). */
      connecterUtilisateur(): Chainable<UtilisateurTest>;
      /** Abonne l'utilisateur au thème de ce nom, par l'API. */
      abonnerA(user: UtilisateurTest, nomDuTheme: string): Chainable<void>;
      /** Crée un article dans le thème de ce nom, par l'API, et le renvoie. */
      creerArticle(
        user: UtilisateurTest,
        nomDuTheme: string,
        titre: string,
      ): Chainable<ArticleTest>;
    }
  }
}

Cypress.Commands.add('creerUtilisateur', () => {
  const identifiants = identifiantsUniques();
  return cy
    .request('POST', `${apiUrl()}/auth/register`, identifiants)
    .then((response) => ({ ...identifiants, token: response.body.token as string }));
});

Cypress.Commands.add('ouvrirEnTantQue', (user: UtilisateurTest, chemin: string) => {
  // Le token est déposé dans le navigateur AVANT le chargement de l'application (onBeforeLoad) :
  // au démarrage, le front le lit comme si l'utilisateur s'était connecté par le formulaire.
  cy.visit(chemin, {
    onBeforeLoad: (win) => win.localStorage.setItem(TOKEN_STORAGE_KEY, user.token),
  });
});

Cypress.Commands.add('connecterUtilisateur', () => {
  return cy.creerUtilisateur().then((user) => {
    cy.ouvrirEnTantQue(user, '/posts');
    return cy.wrap(user);
  });
});

/** Identifiant du thème de ce nom (comparaison exacte : « Java » n'est pas « JavaScript »). */
function idDuTheme(user: UtilisateurTest, nomDuTheme: string): Cypress.Chainable<number> {
  return cy
    .request({
      url: `${apiUrl()}/topics`,
      headers: { Authorization: `Bearer ${user.token}` },
    })
    .then((response) => {
      const theme = (response.body as { id: number; name: string }[]).find(
        (t) => t.name === nomDuTheme,
      );
      if (!theme) {
        throw new Error(`Thème « ${nomDuTheme} » introuvable dans l'API de test`);
      }
      return theme.id;
    });
}

Cypress.Commands.add('abonnerA', (user: UtilisateurTest, nomDuTheme: string) => {
  idDuTheme(user, nomDuTheme).then((topicId) =>
    cy.request({
      method: 'POST',
      url: `${apiUrl()}/users/me/subscriptions/${topicId}`,
      headers: { Authorization: `Bearer ${user.token}` },
    }),
  );
});

Cypress.Commands.add('creerArticle', (user: UtilisateurTest, nomDuTheme: string, titre: string) => {
  return idDuTheme(user, nomDuTheme).then((topicId) =>
    cy
      .request({
        method: 'POST',
        url: `${apiUrl()}/posts`,
        headers: { Authorization: `Bearer ${user.token}` },
        body: { topicId, title: titre, content: `Contenu de « ${titre} »` },
      })
      .then((response) => response.body as ArticleTest),
  );
});
