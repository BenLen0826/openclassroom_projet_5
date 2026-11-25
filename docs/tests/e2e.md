# Plan de test : tests end-to-end (Cypress)

Les tests end-to-end pilotent un vrai navigateur (Chrome) sur l'application complète : front → API → base de données. Ils sont dans `front/cypress/e2e/`. Chaque test est indépendant : il crée son propre compte. L'API utilisée est une API de test, sur une base H2 en mémoire vide à chaque démarrage : la base MySQL n'est jamais touchée.

## Cas de test

| Fonctionnalité | Cas de test | Fichier | Test |
|----------------|-------------|---------|------|
| Accueil | La page d'accueil s'affiche et mène à l'inscription | `home.cy.ts` | `propose de se connecter et de s'inscrire` |
| Inscription | Un visiteur s'inscrit par le formulaire et arrive sur le fil | `auth.cy.ts` | `inscription par le formulaire : l'utilisateur arrive sur le fil` |
| Connexion | Un utilisateur se connecte par son e-mail, puis par son nom d'utilisateur | `auth.cy.ts` | `connexion par e-mail, puis par nom d'utilisateur` |
| Connexion | Un mauvais mot de passe est refusé avec un message | `auth.cy.ts` | `refuse un mauvais mot de passe avec un message` |
| Session persistante | L'utilisateur reste connecté après un rechargement de la page | `auth.cy.ts` | `garde la connexion après un rechargement de la page (session persistante)` |
| Déconnexion | L'utilisateur revient à l'accueil ; les pages privées renvoient à la connexion | `auth.cy.ts` | `déconnexion : retour à l'accueil, les pages privées renvoient à la connexion` |
| Abonnement | S'abonner à un thème : le bouton devient « Déjà abonné », même après rechargement | `topics.cy.ts` | `s'abonner à un thème : le bouton devient « Déjà abonné », même après rechargement` |
| Article | Créer un article : sa page s'ouvre, puis il apparaît dans le fil | `posts.cy.ts` | `créer un article : sa page s'ouvre, puis il apparaît dans le fil` |
| Fil | Trier le fil du plus récent au plus ancien, puis l'inverse | `posts.cy.ts` | `trier le fil : du plus récent au plus ancien, puis l’inverse` |
| Commentaire | Commenter un article : le commentaire s'affiche avec le nom de son auteur | `comments.cy.ts` | `commenter un article : le commentaire s'affiche avec le nom de son auteur` |
| Profil | Modifier son nom d'utilisateur : le nouveau nom est enregistré | `profile.cy.ts` | `modifier son nom d'utilisateur : le nouveau nom est enregistré` |
| Désabonnement | Se désabonner : le thème quitte les abonnements et ses articles quittent le fil | `profile.cy.ts` | `se désabonner : le thème quitte les abonnements et ses articles quittent le fil` |

## Commandes Cypress personnalisées

Les données de départ d'un test (compte connecté, abonnement, articles) sont préparées par des commandes créées pour le projet, dans `front/cypress/support/commands.ts`. Elles appellent directement l'API, sans passer par les formulaires, qui sont testés une seule fois dans leur propre fichier.

| Commande | Rôle | Utilisée dans |
|----------|------|---------------|
| `cy.creerUtilisateur()` | Crée un compte unique par l'API et le renvoie avec son token | `auth.cy.ts`, `posts.cy.ts`, `profile.cy.ts` |
| `cy.ouvrirEnTantQue(user, chemin)` | Ouvre une page de l'application en étant déjà connecté avec ce compte (token déposé dans le navigateur avant le chargement) | `posts.cy.ts`, `profile.cy.ts` |
| `cy.connecterUtilisateur()` | Crée un compte puis ouvre le fil déjà connecté (les deux commandes précédentes à la suite) | `auth.cy.ts`, `topics.cy.ts`, `posts.cy.ts`, `comments.cy.ts`, `profile.cy.ts` |
| `cy.abonnerA(user, nomDuTheme)` | Abonne le compte au thème de ce nom, par l'API | `posts.cy.ts`, `profile.cy.ts` |
| `cy.creerArticle(user, nomDuTheme, titre)` | Crée un article dans ce thème, par l'API, et le renvoie (avec son identifiant) | `posts.cy.ts`, `comments.cy.ts`, `profile.cy.ts` |

## Commandes

| Étape | Dossier | Commande | Rôle |
|-------|---------|----------|------|
| 1 | `back/` | `./mvnw spring-boot:test-run` (PowerShell : `.\mvnw.cmd spring-boot:test-run`) | Lance l'API de test (base H2 en mémoire) ; arrêter d'abord l'API normale et vérifier `jdbc:h2:mem:mdd-e2e` dans le log |
| 2 | `front/` | `npm start` | Lance le front sur `http://localhost:4200` |
| 3 | `front/` | `npm run cypress:run` | Lance tous les tests dans Chrome, sans interface, et affiche le résultat |
| 3 (variante) | `front/` | `npm run cypress:open` | Ouvre l'interface de Cypress pour lancer et suivre les tests pas à pas |
