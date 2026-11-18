import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreatePostRequest, Post } from '../models/post';

/** Appels HTTP des articles. Ne garde aucun état (voir `PostStore`). */
@Injectable({ providedIn: 'root' })
export class PostService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/posts`;

  /** Fil d'actualité, du plus récent au plus ancien. */
  getFeed(): Observable<Post[]> {
    return this.http.get<Post[]>(this.baseUrl);
  }

  create(request: CreatePostRequest): Observable<Post> {
    return this.http.post<Post>(this.baseUrl, request);
  }

  getById(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.baseUrl}/${id}`);
  }
}
