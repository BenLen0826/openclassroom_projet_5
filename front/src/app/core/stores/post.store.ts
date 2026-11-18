import { Injectable, computed, inject, signal } from '@angular/core';
import { Post } from '../models/post';
import { PostService } from '../services/post.service';
import { AuthStore } from './auth.store';

export type SortOrder = 'desc' | 'asc';

const LOAD_ERROR = 'Impossible de charger les articles. Réessayez plus tard.';

/**
 * État du fil d'actualité.
 * <p>
 * L'API renvoie le fil du plus récent au plus ancien ; l'inversion du tri se fait
 * ici, sans nouvelle requête (toutes les données sont déjà chargées, sans pagination).
 * Comme pour les thèmes, la liste est mémorisée avec le token du compte qui l'a
 * chargée : elle n'est jamais montrée à un autre compte.
 */
@Injectable({ providedIn: 'root' })
export class PostStore {
  private readonly postService = inject(PostService);
  private readonly authStore = inject(AuthStore);

  private readonly _loaded = signal<{ token: string | null; posts: Post[] }>({
    token: null,
    posts: [],
  });
  private readonly _order = signal<SortOrder>('desc');
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly order = this._order.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  /** Fil dans l'ordre choisi ; vide si la liste a été chargée pour un autre compte. */
  readonly posts = computed(() => {
    const loaded = this._loaded();
    const posts = loaded.token === this.authStore.token() ? loaded.posts : [];
    return this._order() === 'desc' ? posts : [...posts].reverse();
  });

  load(): void {
    const token = this.authStore.token();
    this._loading.set(true);
    this._error.set(null);
    this.postService.getFeed().subscribe({
      next: (posts) => {
        this._loaded.set({ token, posts });
        this._loading.set(false);
      },
      error: () => {
        this._error.set(LOAD_ERROR);
        this._loading.set(false);
      },
    });
  }

  /** Du plus récent au plus ancien, ou l'inverse. */
  toggleOrder(): void {
    this._order.update((order) => (order === 'desc' ? 'asc' : 'desc'));
  }
}
