/** Profil de l'utilisateur connecté (`GET /api/users/me`, aligné sur `UserDto`). */
export interface User {
  id: number;
  username: string;
  email: string;
}

/** Données du formulaire du profil (`PUT /api/users/me`). Mot de passe vide = inchangé. */
export interface UpdateProfileRequest {
  username: string;
  email: string;
  password: string;
}
