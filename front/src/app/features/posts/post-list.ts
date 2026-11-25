import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PostStore } from '../../core/stores/post.store';
import { PostCard } from '../../shared/components/post-card';

/** Page « Articles » : fil d'actualité avec tri par date. */
@Component({
  selector: 'app-post-list',
  imports: [RouterLink, PostCard],
  templateUrl: './post-list.html',
  styleUrl: './post-list.scss',
})
export class PostList implements OnInit {
  protected readonly store = inject(PostStore);

  ngOnInit(): void {
    this.store.load();
  }
}
