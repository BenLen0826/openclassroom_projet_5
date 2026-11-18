import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UpdateProfileRequest, User } from '../models/user';

/** Appels HTTP du profil de l'utilisateur connecté (« me » : déduit du token). */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/users/me`;

  getMe(): Observable<User> {
    return this.http.get<User>(this.url);
  }

  updateMe(request: UpdateProfileRequest): Observable<User> {
    return this.http.put<User>(this.url, request);
  }
}
