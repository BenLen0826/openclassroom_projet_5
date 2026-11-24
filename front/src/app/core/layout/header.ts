import { NgTemplateOutlet } from '@angular/common';
import {
  Component,
  DestroyRef,
  ElementRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthStore } from '../stores/auth.store';

/** Forme de l'en-tête : complète (connecté), logo seul (connexion, inscription) ou aucune. */
type HeaderKind = 'full' | 'logo-only' | 'none';

/** Pages d'un visiteur dont l'en-tête se réduit au logo (en desktop seulement, voir le SCSS). */
const LOGO_ONLY_PATHS = ['/login', '/register'];

/**
 * Requête média de la version ordinateur, pour le code TypeScript. Même valeur que
 * `$breakpoint-desktop` (src/styles/_breakpoints.scss), qu'il est impossible de lire depuis
 * TypeScript : les deux sont à modifier ensemble.
 */
const DESKTOP_MEDIA_QUERY = '(min-width: 768px)';

/**
 * En-tête de l'application (Annexe A.2), qui décide seul de sa forme selon la connexion et la page.
 * - Connecté : logo et liens en desktop ; en mobile, bouton burger qui ouvre un panneau
 *   latéral, un <dialog> natif ouvert en modal : focus, touche Échap et affichage du fond
 *   assombri gérés par le navigateur ; fermeture au clic sur le fond et à l'élargissement
 *   de l'écran codées ici.
 * - Visiteur sur la connexion ou l'inscription : logo seul. Ailleurs (accueil) : rien.
 */
@Component({
  selector: 'app-header',
  imports: [NgTemplateOutlet, RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  /** Absent quand l'en-tête n'est pas complet. */
  private readonly menu = viewChild<ElementRef<HTMLDialogElement>>('menu');

  /** Chemin de la page courante, sans paramètres (?…) ni fragment (#…). */
  private readonly path = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(() => pathOf(this.router.url)),
    ),
    { initialValue: pathOf(this.router.url) },
  );

  protected readonly kind = computed<HeaderKind>(() => {
    if (this.authStore.isAuthenticated()) {
      return 'full';
    }
    return LOGO_ONLY_PATHS.includes(this.path()) ? 'logo-only' : 'none';
  });

  /** Menu mobile ouvert (pour `aria-expanded` du bouton burger). */
  protected readonly menuOpen = signal(false);

  constructor() {
    // Menu ouvert puis écran élargi (téléphone tourné en paysage) : le menu mobile n'a plus
    // lieu d'être et laisserait la page inerte derrière le fond assombri, il est donc fermé
    const desktop = window.matchMedia(DESKTOP_MEDIA_QUERY);
    const closeOnDesktop = (event: { matches: boolean }) => {
      if (event.matches) {
        this.closeMenu();
      }
    };
    desktop.addEventListener('change', closeOnDesktop);
    inject(DestroyRef).onDestroy(() => desktop.removeEventListener('change', closeOnDesktop));
  }

  protected openMenu(): void {
    this.menu()?.nativeElement.showModal();
    this.menuOpen.set(true);
  }

  protected closeMenu(): void {
    // L'événement « close » du <dialog> remet menuOpen à false (y compris après Échap)
    const dialog = this.menu()?.nativeElement;
    if (dialog?.open) {
      dialog.close();
    }
  }

  /** Un clic sur le fond assombri a pour cible le <dialog> lui-même, pas le panneau. */
  protected closeOnBackdrop(event: MouseEvent): void {
    if (event.target === this.menu()?.nativeElement) {
      this.closeMenu();
    }
  }

  /** Déconnexion, puis retour à l'accueil. */
  protected logout(): void {
    this.closeMenu();
    this.authStore.logout();
    void this.router.navigateByUrl('/');
  }
}

function pathOf(url: string): string {
  return url.split(/[?#]/)[0];
}
