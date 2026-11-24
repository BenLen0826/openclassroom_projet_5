import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BackLink } from '../../shared/components/back-link';
import { Post } from '../../core/models/post';
import { PostService } from '../../core/services/post.service';
import { CommentSection } from './comment-section';

const NOT_FOUND = 'Article introuvable.';
const LOAD_ERROR = "Impossible de charger l'article. Réessayez plus tard.";

/**
 * Page d'un article (sans mise en forme avant l'étape 6). Les commentaires sont gérés
 * par `CommentSection`. Les données ne servent qu'à cette page : pas de store.
 */
@Component({
  selector: 'app-post-detail',
  imports: [DatePipe, BackLink, CommentSection],
  templateUrl: './post-detail.html',
})
export class PostDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly postService = inject(PostService);

  protected readonly post = signal<Post | null>(null);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      // Adresse invalide (ex. /posts/abc) : inutile d'appeler l'API
      this.error.set(NOT_FOUND);
      return;
    }
    this.postService.getById(id).subscribe({
      next: (post) => this.post.set(post),
      error: (error: unknown) =>
        this.error.set(
          error instanceof HttpErrorResponse && error.status === 404 ? NOT_FOUND : LOAD_ERROR,
        ),
    });
  }
}
