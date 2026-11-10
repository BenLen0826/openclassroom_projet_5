import { Routes } from '@angular/router';
import { Home } from './pages/home/home';

// consider a guard combined with canMatch / canActivate route option
// to manage unauthenticated user to access private routes
export const routes: Routes = [{ path: '', component: Home }];
