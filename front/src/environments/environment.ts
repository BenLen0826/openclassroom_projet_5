// Configuration de production (ng build).
// MVP interne : l'API tourne sur la même machine ; à adapter lors d'un déploiement.
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api',
};
