import { Component, OnInit, inject } from '@angular/core';
import { TopicStore } from '../../core/stores/topic.store';
import { TopicCard } from '../../shared/components/topic-card';

/** Page « Thèmes » : tous les thèmes, avec l'abonnement (sans mise en forme avant l'étape 6). */
@Component({
  selector: 'app-topic-list',
  imports: [TopicCard],
  templateUrl: './topic-list.html',
})
export class TopicList implements OnInit {
  protected readonly store = inject(TopicStore);

  ngOnInit(): void {
    this.store.load();
  }
}
