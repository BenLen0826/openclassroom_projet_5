import { Component, input } from '@angular/core';
import { Comment } from '../../core/models/comment';

/** Un commentaire : nom d'utilisateur et contenu (composant « Commentaire » de l'Annexe A.3). */
@Component({
  selector: 'app-comment-item',
  templateUrl: './comment-item.html',
})
export class CommentItem {
  readonly comment = input.required<Comment>();
}
