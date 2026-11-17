import { Routes } from '@angular/router';
import { Home } from './pages/home/home';

// Les guards (routes privées / pages réservées aux visiteurs) arrivent avec l'authentification, à l'étape 5.
export const routes: Routes = [
  { path: '', component: Home },
  // Page chargée à la demande ; publique tant que l'authentification n'existe pas (étape 5).
  {
    path: 'topics',
    loadComponent: () => import('./features/topics/topic-list').then((m) => m.TopicList),
  },
];
