import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Appels HTTP des abonnements de l'utilisateur connecté (« me » : déduit du token).
 * Ne garde aucun état (voir `TopicStore`).
 */
@Injectable({ providedIn: 'root' })
export class SubscriptionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users/me/subscriptions`;

  subscribe(topicId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${topicId}`, null);
  }

  unsubscribe(topicId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${topicId}`);
  }
}
