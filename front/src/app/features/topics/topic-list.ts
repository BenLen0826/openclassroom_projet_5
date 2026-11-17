import { Component, OnInit, inject } from '@angular/core';
import { TopicStore } from '../../core/stores/topic.store';

/** Page « Thèmes » : liste de tous les thèmes (étape 4, sans mise en forme). */
@Component({
  selector: 'app-topic-list',
  templateUrl: './topic-list.html',
})
export class TopicList implements OnInit {
  protected readonly store = inject(TopicStore);

  ngOnInit(): void {
    this.store.load();
  }
}
