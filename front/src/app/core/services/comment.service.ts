import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comment } from '../models/comment';

/** Appels HTTP des commentaires d'un article. Ne garde aucun état. */
@Injectable({ providedIn: 'root' })
export class CommentService {
  private readonly http = inject(HttpClient);

  private url(postId: number): string {
    return `${environment.apiUrl}/posts/${postId}/comments`;
  }

  /** Commentaires de l'article, du plus ancien au plus récent. */
  getForPost(postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(this.url(postId));
  }

  /** Seul le contenu est envoyé : l'auteur et la date sont définis par le serveur. */
  create(postId: number, content: string): Observable<Comment> {
    return this.http.post<Comment>(this.url(postId), { content });
  }
}
