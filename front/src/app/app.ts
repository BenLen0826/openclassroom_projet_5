import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthStore } from './core/stores/auth.store';

@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  /** Déconnexion, puis retour à l'accueil. */
  protected logout(): void {
    this.authStore.logout();
    void this.router.navigateByUrl('/');
  }
}
