import { Routes } from '@angular/router';
import { Home } from './pages/home/home';

// Les guards (routes privées / pages réservées aux visiteurs) arrivent avec la connexion (feature/login).
export const routes: Routes = [
  { path: '', component: Home },
  // Pages chargées à la demande
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register').then((m) => m.Register),
  },
  {
    path: 'topics',
    loadComponent: () => import('./features/topics/topic-list').then((m) => m.TopicList),
  },
];
