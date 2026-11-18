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

### Données de démonstration (profil `dev` uniquement)

Lancée avec le profil `dev`, l'API ajoute au premier démarrage deux comptes, leurs abonnements, trois articles et deux commentaires (rien n'est ajouté si les comptes existent déjà). Ces comptes ont un mot de passe connu : **ne jamais utiliser le profil `dev` en conditions réelles**.

| Nom d'utilisateur | E-mail | Mot de passe | Thèmes suivis |
|-------------------|--------|--------------|---------------|
| `alice` | `alice@example.com` | `Demo1234!` | Java, Angular |
| `bob` | `bob@example.com` | `Demo1234!` | Java, Spring |

## Front

Application Angular 22 (Angular CLI 22.2.6, Angular Material), dans le dossier `front/`.

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
| `npm run cypress:run` | Lance les tests end-to-end Cypress dans Chrome, sans interface (l'application doit tourner : `npm start` dans un autre terminal) |
| `npm run cypress:open` | Ouvre l'interface de Cypress pour lancer et suivre les tests pas à pas |
