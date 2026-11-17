import { Injectable, inject, signal } from '@angular/core';
import { Topic } from '../models/topic';
import { TopicService } from '../services/topic.service';

/**
 * État des thèmes, partagé entre les pages (thèmes, puis profil à l'étape 5).
 * <p>
 * L'état est gardé dans des signals privés, exposé en lecture seule, et modifié
 * uniquement par les méthodes du store.
 */
@Injectable({ providedIn: 'root' })
export class TopicStore {
  private readonly topicService = inject(TopicService);

  private readonly _topics = signal<Topic[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly topics = this._topics.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  load(): void {
    this._loading.set(true);
    this._error.set(null);
    this.topicService.getAll().subscribe({
      next: (topics) => {
        this._topics.set(topics);
        this._loading.set(false);
      },
      error: () => {
        // Message simple pour l'utilisateur ; la gestion globale des erreurs arrive à l'étape 5.
        this._error.set('Impossible de charger les thèmes. Réessayez plus tard.');
        this._loading.set(false);
      },
    });
  }
}
