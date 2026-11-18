import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthStore } from '../stores/auth.store';

const AUTH_URL = `${environment.apiUrl}/auth/`;

/** Requête vers notre API, hors inscription et connexion (`/auth/`). */
function isProtectedApiCall(url: string): boolean {
  return url.startsWith(`${environment.apiUrl}/`) && !url.startsWith(AUTH_URL);
}

/**
 * Ajoute le JWT aux appels de l'API et déconnecte l'utilisateur si l'API le refuse.
 * <p>
 * - Le token n'est envoyé qu'à notre API, jamais vers un autre site.
 * - Il n'est jamais envoyé à `/auth/` : un token périmé y ferait refuser la connexion
 *   par le module de sécurité, avant même le contrôleur.
 * - Un `401` (token absent, invalide ou expiré) déconnecte l'utilisateur et le renvoie
 *   vers la page de connexion. Un `401` sur `/auth/login` (identifiants incorrects) est
 *   laissé à la page de connexion, qui affiche le message.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authStore = inject(AuthStore);
  const router = inject(Router);
  const protectedCall = isProtectedApiCall(request.url);

  // Token lu à chaque requête : après une déconnexion, plus aucun en-tête n'est envoyé
  const token = authStore.token();
  const authorizedRequest =
    protectedCall && token
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;

  return next(authorizedRequest).pipe(
    catchError((error: unknown) => {
      if (protectedCall && error instanceof HttpErrorResponse && error.status === 401) {
        authStore.logout();
        void router.navigateByUrl('/login');
      }
      return throwError(() => error);
    }),
  );
};
