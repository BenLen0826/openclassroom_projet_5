import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { LoginRequest, RegisterRequest } from '../models/auth';
import { AuthService } from '../services/auth.service';

/** Clé du token dans `localStorage` : il survit à la fermeture du navigateur (connexion persistante). */
export const TOKEN_STORAGE_KEY = 'mdd_token';

/**
 * État de l'authentification : le token de l'utilisateur connecté.
 * <p>
 * Le token est gardé dans un signal privé, exposé en lecture seule, et recopié
 * dans `localStorage` pour rester connecté entre deux visites.
 */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly authService = inject(AuthService);

  private readonly _token = signal<string | null>(localStorage.getItem(TOKEN_STORAGE_KEY));

  readonly token = this._token.asReadonly();
  readonly isAuthenticated = computed(() => this._token() !== null);

  /**
   * Inscrit l'utilisateur et le connecte directement. Renvoie un `Observable` pour
   * que la page gère la navigation et l'affichage des erreurs.
   */
  register(request: RegisterRequest): Observable<void> {
    return this.authService.register(request).pipe(
      tap((response) => this.saveToken(response.token)),
      map(() => undefined),
    );
  }

  /** Connecte l'utilisateur (e-mail ou nom d'utilisateur). Même principe que `register`. */
  login(request: LoginRequest): Observable<void> {
    return this.authService.login(request).pipe(
      tap((response) => this.saveToken(response.token)),
      map(() => undefined),
    );
  }

  /** Déconnecte l'utilisateur : le token est oublié (signal et `localStorage`). */
  logout(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    this._token.set(null);
  }

  /** Enregistre le token reçu : signal + `localStorage`. */
  private saveToken(token: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
    this._token.set(token);
  }
}
