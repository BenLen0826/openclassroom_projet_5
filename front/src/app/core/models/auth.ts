/** Données du formulaire d'inscription (`POST /api/auth/register`). */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

/** Réponse de l'inscription et de la connexion : le JWT. */
export interface AuthResponse {
  token: string;
}
