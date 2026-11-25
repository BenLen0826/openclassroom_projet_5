# Sécurité de MDD : cas de test

Ce document liste les protections de l'application MDD et le test qui prouve chacune d'elles. Les rapports de test détaillés (couverture, résultats complets) font l'objet d'un livrable séparé.

## Principe

L'interface Angular tourne dans le navigateur de l'utilisateur : n'importe qui peut la contourner en appelant l'API directement (Postman, script). La vraie protection des données est donc **l'API**, qui refuse toute requête sans token JWT valide. Chaque cas de test ci-dessous simule une attaque ou un risque et vérifie qu'il échoue.

## Cas de test

### Accès à l'API

| Ce qui est vérifié | Pourquoi | Preuve |
|--------------------|----------|--------|
| Les 10 routes protégées refusent une requête sans token (`401`) | Empêcher de lire ou de modifier des données en appelant l'API directement | `SecurityIntegrationTest` → `everyProtectedRoute_withoutToken_returns401` |
| Toute route non déclarée publique exige un token (refus par défaut) | Une route ajoutée plus tard est protégée sans configuration supplémentaire | `SecurityIntegrationTest` → `protectedRoute_withoutToken_returns401` (route qui n'est pas dans la liste des routes publiques) |
| Un token signé avec une autre clé est refusé | Un attaquant peut fabriquer un token en se déclarant « utilisateur n° 1 » : seule la signature, produite avec la clé secrète du serveur, l'en empêche | `SecurityIntegrationTest` → `protectedRoute_withTokenSignedByAnotherKey_returns401` |
| Un texte qui n'est pas un token est refusé | Faux en-tête `Authorization` | `SecurityIntegrationTest` → `protectedRoute_withInvalidToken_returns401` |
| Un token expiré est refusé | Réutilisation d'un ancien token volé | `SecurityIntegrationTest` → `protectedRoute_withExpiredToken_returns401` |
| Le token d'un compte supprimé est refusé | Token encore valide d'un utilisateur qui n'existe plus | `TopicControllerIntegrationTest` → `getAllTopics_withTokenOfDeletedUser_returns401` |
| L'utilisateur vient toujours du token, jamais de l'URL ni du corps de la requête | Empêcher de modifier un profil ou de publier au nom d'un autre | Routes `/api/users/me` (`UserControllerIntegrationTest`) ; auteur envoyé par le client ignoré (`PostControllerIntegrationTest`, `CommentControllerIntegrationTest`) |
| L'API ne crée aucune session | Une session par cookie rendrait l'API sensible aux attaques CSRF | `SecurityIntegrationTest` → `register_isPublicWithoutCsrfTokenAndCreatesNoSession` |
| Seule l'origine du front peut appeler l'API depuis un navigateur (CORS) | Un autre site ne doit pas pouvoir utiliser l'API à la place de l'utilisateur | `TopicControllerIntegrationTest` → `cors_allowsFrontOrigin`, `cors_rejectsUnknownOrigin` |

### Mots de passe et identifiants

| Ce qui est vérifié | Pourquoi | Preuve |
|--------------------|----------|--------|
| Le mot de passe n'est jamais renvoyé par l'API, même haché | Ne pas exposer le hash, qui pourrait être attaqué hors ligne | `AuthControllerIntegrationTest` → `register_returns201WithTokenAndNeverThePassword` ; `UserControllerIntegrationTest` → `getMe_returnsProfileWithoutPassword` |
| Le mot de passe est haché en base (BCrypt) | En cas de fuite de la base, les mots de passe ne sont pas lisibles | `AuthControllerIntegrationTest` → `register_storesHashedPassword` |
| Une connexion refusée donne le même message, que le compte existe ou non | Ne pas révéler, à la connexion, quelles adresses e-mail ont un compte | `AuthControllerIntegrationTest` → `login_withWrongPassword_returns401WithGenericMessage`, `login_withUnknownUser_returns401WithSameMessage` |
| L'API refuse de démarrer sans clé JWT d'au moins 32 caractères, ou avec la valeur d'exemple | Une clé faible ou connue permettrait de fabriquer des tokens | `JwtPropertiesTest` |

### Données personnelles dans les logs

| Ce qui est vérifié | Pourquoi | Preuve |
|--------------------|----------|--------|
| Ni e-mail, ni mot de passe, ni token dans les logs (inscription, connexion réussie et refusée, article, e-mail déjà utilisé) | Quiconque lit les journaux du serveur y trouverait des données personnelles (RGPD) | Essai réel sur MySQL (25/11/2026) : aucune de ces valeurs dans la console de l'API |

### Front-end

| Ce qui est vérifié | Pourquoi | Preuve |
|--------------------|----------|--------|
| Le token n'est envoyé qu'à notre API, jamais à un autre site | Ne pas transmettre le token à un domaine tiers | `auth.interceptor.spec.ts` → `never sends the token to another domain` |
| Un `401` de l'API déconnecte l'utilisateur | Ne pas rester « connecté » avec un token refusé | `auth.interceptor.spec.ts` → `logs out and redirects to /login on a 401 from the API` |
| Le token ne passe jamais dans une URL | Il serait visible dans l'historique, un lien partagé ou un journal | Recherche dans le code : le token n'est utilisé que dans l'en-tête `Authorization` |
| Aucun `console.log` dans le code | Un token ou des données affichés dans la console du navigateur seraient visibles de tous | Règle ESLint `no-console` : `npm run lint` refuse tout `console.log` |
| Aucun `innerHTML`, `bypassSecurityTrust…` ni `DomSanitizer` | Faille XSS : du code injecté pourrait lire le token (bonnes pratiques de sécurité Angular) | Recherche dans le code : aucun résultat |

### Secrets

| Ce qui est vérifié | Pourquoi | Preuve |
|--------------------|----------|--------|
| Le fichier `.env` (base de données, clé JWT) n'est jamais versionné | Ne pas publier de secrets sur GitHub | `.env` ignoré par Git ; `.env.example` sans vraie valeur |

## Limites connues

| Limite | Raison | Amélioration possible |
|--------|--------|-----------------------|
| La documentation Swagger est publique | MVP déployé en interne ; elle ne donne accès à aucune donnée, chaque route demande un token | La réserver au profil `dev` avant toute mise en ligne publique |
| Un token reste valable 7 jours ; la déconnexion ne l'invalide pas côté serveur | Connexion persistante demandée, MVP | Token d'accès court et refresh token, liste de révocation |
| Le token est stocké dans `localStorage`, lisible par JavaScript | Simplicité ; protégé par l'absence de faille XSS | Cookie `HttpOnly` |
| L'inscription signale qu'un e-mail est déjà utilisé (`409`), ce qui révèle qu'il a un compte | Ergonomie : le message s'affiche sous le champ concerné | Confirmation de l'inscription par e-mail |
