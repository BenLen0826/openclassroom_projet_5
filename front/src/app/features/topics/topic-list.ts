import { Component, OnInit, inject } from '@angular/core';
import { TopicStore } from '../../core/stores/topic.store';
import { TopicCard } from '../../shared/components/topic-card';

/** Page « Thèmes » : tous les thèmes, avec l'abonnement. */
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
