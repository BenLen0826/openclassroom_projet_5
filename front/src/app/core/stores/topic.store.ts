import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { Topic } from '../models/topic';
import { SubscriptionService } from '../services/subscription.service';
import { TopicService } from '../services/topic.service';
import { AuthStore } from './auth.store';

const LOAD_ERROR = 'Impossible de charger les thèmes. Réessayez plus tard.';
const ACTION_ERROR = "Impossible de modifier l'abonnement. Réessayez plus tard.";

/**
 * État des thèmes et des abonnements, partagé par la page des thèmes et le profil.
 * <p>
 * L'état est gardé dans des signals privés, exposé en lecture seule, et modifié
 * uniquement par les méthodes du store.
 */
@Injectable({ providedIn: 'root' })
export class TopicStore {
  private readonly topicService = inject(TopicService);
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly authStore = inject(AuthStore);

  /**
   * Liste chargée, mémorisée avec le token du compte pour lequel elle a été chargée :
   * après une déconnexion ou un changement de compte, elle n'est plus exposée
   * (les abonnements d'un compte ne s'affichent jamais pour un autre).
   */
  private readonly _loaded = signal<{ token: string | null; topics: Topic[] }>({
    token: null,
    topics: [],
  });
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);
  private readonly _actionError = signal<string | null>(null);
  private readonly _pending = signal<ReadonlySet<number>>(new Set());

  readonly topics = computed(() => {
    const loaded = this._loaded();
    return loaded.token === this.authStore.token() ? loaded.topics : [];
  });
  readonly subscribedTopics = computed(() => this.topics().filter((topic) => topic.subscribed));
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly actionError = this._actionError.asReadonly();

  load(): void {
    const token = this.authStore.token();
    this._loading.set(true);
    this._error.set(null);
    // Une erreur d'abonnement passée ne doit pas réapparaître sur une autre page ou un autre compte
    this._actionError.set(null);
    this.topicService.getAll().subscribe({
      next: (topics) => {
        this._loaded.set({ token, topics });
        this._loading.set(false);
      },
      error: () => {
        this._error.set(LOAD_ERROR);
        this._loading.set(false);
      },
    });
  }

  /** `true` pendant l'envoi d'un abonnement ou d'un désabonnement pour ce thème. */
  isPending(topicId: number): boolean {
    return this._pending().has(topicId);
  }

  subscribe(topicId: number): void {
    this.changeSubscription(topicId, true, () => this.subscriptionService.subscribe(topicId));
  }

  unsubscribe(topicId: number): void {
    this.changeSubscription(topicId, false, () => this.subscriptionService.unsubscribe(topicId));
  }

  /**
   * Envoie la demande puis met à jour le seul thème concerné, sans recharger la liste.
   * Une seconde demande pour le même thème est ignorée tant que la première est en cours.
   */
  private changeSubscription(
    topicId: number,
    subscribed: boolean,
    request: () => Observable<void>,
  ): void {
    if (this.isPending(topicId)) {
      return;
    }
    this._actionError.set(null);
    this.setPending(topicId, true);
    request().subscribe({
      next: () => {
        this._loaded.update((loaded) => ({
          ...loaded,
          topics: loaded.topics.map((topic) =>
            topic.id === topicId ? { ...topic, subscribed } : topic,
          ),
        }));
        this.setPending(topicId, false);
      },
      error: () => {
        this._actionError.set(ACTION_ERROR);
        this.setPending(topicId, false);
      },
    });
  }

  private setPending(topicId: number, pending: boolean): void {
    this._pending.update((current) => {
      const next = new Set(current);
      if (pending) {
        next.add(topicId);
      } else {
        next.delete(topicId);
      }
      return next;
    });
  }
}
