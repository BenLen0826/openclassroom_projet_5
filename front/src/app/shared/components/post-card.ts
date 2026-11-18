import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Post } from '../../core/models/post';

/** Carte d'un article du fil (« Carte d'article » de l'Annexe A.3) : mène à la page de l'article. */
@Component({
  selector: 'app-post-card',
  imports: [DatePipe, RouterLink],
  templateUrl: './post-card.html',
})
export class PostCard {
  readonly post = input.required<Post>();
}
