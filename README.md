# P6-Full-Stack-reseau-dev

## Back

API Spring Boot 4.1 (Java 21), dans le dossier `back/`.

### Configuration

Les identifiants de la base et la clé JWT sont lus dans des variables d'environnement. Pour un usage local :

1. Copier `back/.env.example` en `back/.env` (ce fichier est ignoré par Git).
2. Renseigner `DB_USERNAME`, `DB_PASSWORD` et `JWT_SECRET` (au moins 32 caractères).

Une variable définie dans le système est prioritaire sur le fichier `.env`.

### Commandes (depuis `back/`)

| Commande | Rôle |
|----------|------|
| `./mvnw spring-boot:run` | Lance l'API sur `http://localhost:8080` |
| `./mvnw test` | Lance les tests (base H2 en mémoire, profil `test`) et génère le rapport de couverture `target/site/jacoco/index.html` |
| `./mvnw verify` | Idem, et échoue si la couverture des lignes est inférieure à 70 % |

## Front

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 22.2.6.

Don't forget to install your node_modules before starting (`npm install`).

### Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

### Where to start

As you may have seen if you already started the app, a simple home page containing a logo, a title and a button is available. If you take a look at its code (in `src/app/pages/home/home.html`) you will see that an external UI library is already configured in the project.

This library is `@angular/material`, it's one of the most famous in the angular ecosystem. As you can see on their docs (https://material.angular.io/), it contains a lot of highly customizable components that will help you design your interfaces quickly.

Note: I recommend to use material however it's not mandatory, if you prefer you can get rid of it.

Good luck!
