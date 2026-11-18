import { Component, OnInit, inject } from '@angular/core';
import { TopicStore } from '../../core/stores/topic.store';
import { TopicCard } from '../../shared/components/topic-card';

/**
 * Page « Profil », minimale pour l'instant : la section « Abonnements » permet de
 * se désabonner. La consultation et la modification du profil arrivent avec
 * `feature/profile` (sans mise en forme avant l'étape 6).
 */
@Component({
  selector: 'app-profile',
  imports: [TopicCard],
  templateUrl: './profile.html',
})
export class Profile implements OnInit {
  protected readonly store = inject(TopicStore);

  ngOnInit(): void {
    this.store.load();
  }
}
