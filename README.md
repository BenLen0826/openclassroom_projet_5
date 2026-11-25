# MDD (Monde de Dév)

## Back

API Spring Boot 4.1 (Java 21), dans le dossier `back/`.

### Base de données

Prérequis : MySQL 8 ou plus récent. Le script `back/sql/init-database.sql` crée la base `mdd` (utf8mb4) et le compte `mdd_user`, limité à cette base et aux seuls droits dont l'API a besoin.

> ⚠️ Avant de le lancer, remplacer `CHANGER_MOI` dans le script par le mot de passe choisi pour `mdd_user` (le même que `DB_PASSWORD` dans `back/.env`), sans commiter cette modification.

Il se lance une fois en root, depuis la racine du projet. Dans le client `mysql` connecté en root (`mysql -u root -p`) :

```
SOURCE back/sql/init-database.sql
```

Les tables ne sont pas créées par le script : Hibernate les génère au démarrage de l'API.

### Configuration

Les identifiants de la base et la clé JWT sont lus dans des variables d'environnement. Pour un usage local :

1. Copier `back/.env.example` en `back/.env` (ce fichier est ignoré par Git).
2. Renseigner `DB_USERNAME`, `DB_PASSWORD` et `JWT_SECRET` (au moins 32 caractères).

Une variable définie dans le système est prioritaire sur le fichier `.env`.

### Commandes (depuis `back/`)

| Commande | Rôle |
|----------|------|
| `./mvnw spring-boot:run` | Lance l'API sur `http://localhost:8080` |
| `./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"` | Idem, avec les données de démonstration (voir ci-dessous). Les guillemets sont **obligatoires sous PowerShell** (`.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"`), qui couperait sinon l'argument au niveau du point |
| `./mvnw test` | Lance les tests (base H2 en mémoire, profil `test`) et génère le rapport de couverture `target/site/jacoco/index.html` |
| `./mvnw verify` | Idem, et échoue si la couverture des lignes est inférieure à 70 % |
| `./mvnw spring-boot:test-run` | Lance l'API pour les tests end-to-end (profil `e2e` toujours actif), sur une base H2 en mémoire vide à chaque démarrage (voir [Tests end-to-end](#tests-end-to-end-cypress)) |

### Données de démonstration (profil `dev` uniquement)

Lancée avec le profil `dev`, l'API ajoute au premier démarrage deux comptes, leurs abonnements, trois articles et deux commentaires (rien n'est ajouté si les comptes existent déjà). Ces comptes ont un mot de passe connu : **ne jamais utiliser le profil `dev` en conditions réelles**.

| Nom d'utilisateur | E-mail | Mot de passe | Thèmes suivis |
|-------------------|--------|--------------|---------------|
| `user1` | `user1@example.com` | `Demo1234!` | Java, Angular |
| `user2` | `user2@example.com` | `Demo1234!` | Java, Spring |

## Front

Application Angular 22 (Angular CLI 22.2.6, styles en SCSS), dans le dossier `front/`.

Installer les dépendances avant le premier lancement : `npm install`.

### Configuration

L'URL de l'API est définie dans `src/environments/environment.development.ts` (utilisé par `ng serve`) et `src/environments/environment.ts` (utilisé par `ng build`). Par défaut : `http://localhost:8080/api`.

### Commandes (depuis `front/`)

| Commande | Rôle |
|----------|------|
| `npm start` | Lance l'application sur `http://localhost:4200` |
| `npm test` | Lance les tests unitaires (Vitest) |
| `npx ng test --watch=false --coverage` | Lance les tests une fois et génère le rapport de couverture `coverage/` |
| `npm run lint` | Analyse le code avec ESLint (TypeScript, Angular, accessibilité des templates) |
| `npm run build` | Compile l'application dans `dist/` |
| `npm run cypress:run` | Lance les tests end-to-end Cypress dans Chrome, sans interface (voir ci-dessous) |
| `npm run cypress:open` | Ouvre l'interface de Cypress pour lancer et suivre les tests pas à pas |

### Tests end-to-end (Cypress)

Les tests end-to-end pilotent un vrai navigateur (Chrome) sur l'application complète : front → API → base de données. Ils sont dans `front/cypress/e2e/` :

| Fichier | Ce qu'il vérifie |
|---------|------------------|
| `home.cy.ts` | La page d'accueil s'affiche et mène à l'inscription (n'a besoin que du front) |
| `auth.cy.ts` | Inscription, connexion par e-mail et par nom d'utilisateur, mauvais mot de passe, session gardée après rechargement, déconnexion |
| `topics.cy.ts` | Abonnement à un thème |
| `posts.cy.ts` | Création d'un article, fil d'actualité, tri du fil |
| `comments.cy.ts` | Commentaire sur un article |
| `profile.cy.ts` | Modification du profil, désabonnement |

Chaque test est indépendant : il crée son propre compte. Les commandes de `cypress/support/commands.ts` (`cy.connecterUtilisateur()`, `cy.abonnerA()`, `cy.creerArticle()`…) préparent ces données en appelant directement l'API, sans repasser par les formulaires.

Les tests créent de vrais comptes et articles. Ils utilisent donc une **API de test**, sur une base H2 en mémoire vide à chaque démarrage (seuls les thèmes sont insérés) : la base MySQL n'est jamais touchée. Chaque lancement crée des données uniques : on peut relancer les tests sans redémarrer l'API.

1. **Arrêter l'API normale** si elle tourne (le port 8080 doit être libre).
2. Lancer l'API de test, depuis `back/` : `./mvnw spring-boot:test-run` (sous PowerShell : `.\mvnw.cmd spring-boot:test-run`). Le profil `e2e` est activé automatiquement.
3. **Vérifier dans le log de l'API de test** la ligne `Database JDBC URL [jdbc:h2:mem:mdd-e2e]` (et « Thèmes de référence : 8 ajouté(s) »). Si l'API s'arrête avec « Port 8080 was already in use », l'API normale tourne encore : l'arrêter, sinon les tests écriraient dans la base MySQL.
4. Lancer le front, depuis `front/` : `npm start`.
5. Lancer les tests, depuis `front/` : `npm run cypress:run` (ou `npm run cypress:open` pour les suivre pas à pas).

⚠️ Utiliser `spring-boot:test-run` et non `spring-boot:run` : ce dernier ne voit pas la configuration de test et se connecterait à la base MySQL.
