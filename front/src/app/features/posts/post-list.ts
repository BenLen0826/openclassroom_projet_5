import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PostStore } from '../../core/stores/post.store';

/** Page « Articles » : fil d'actualité avec tri par date (sans mise en forme avant l'étape 6). */
@Component({
  selector: 'app-post-list',
  imports: [DatePipe, RouterLink],
  templateUrl: './post-list.html',
})
export class PostList implements OnInit {
  protected readonly store = inject(PostStore);

  ngOnInit(): void {
    this.store.load();
  }
}
