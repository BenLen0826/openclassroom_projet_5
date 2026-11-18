import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { Home } from './features/auth/home';

// guestGuard : pages réservées aux visiteurs (un utilisateur connecté va au fil d'actualité).
// authGuard : pages privées (un visiteur va à la connexion). Pages chargées à la demande sauf l'accueil.
export const routes: Routes = [
  { path: '', component: Home, canActivate: [guestGuard] },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register').then((m) => m.Register),
  },
  {
    path: 'posts',
    canActivate: [authGuard],
    loadComponent: () => import('./features/posts/post-list').then((m) => m.PostList),
  },
  // « new » avant « :id », sinon « new » serait pris pour l'identifiant d'un article
  {
    path: 'posts/new',
    canActivate: [authGuard],
    loadComponent: () => import('./features/posts/post-create').then((m) => m.PostCreate),
  },
  {
    path: 'posts/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/posts/post-detail').then((m) => m.PostDetail),
  },
  {
    path: 'topics',
    canActivate: [authGuard],
    loadComponent: () => import('./features/topics/topic-list').then((m) => m.TopicList),
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile').then((m) => m.Profile),
  },
  { path: '**', redirectTo: '' },
];
