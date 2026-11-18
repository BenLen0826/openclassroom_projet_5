import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { AuthStore } from '../stores/auth.store';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  /** Exécute le guard, avec ou sans token déjà enregistré. */
  function runGuard(token: string | null, logoutFirst = false): boolean | UrlTree {
    localStorage.clear();
    if (token) {
      localStorage.setItem('mdd_token', token);
    }
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    if (logoutFirst) {
      TestBed.inject(AuthStore).logout();
    }
    return TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    ) as boolean | UrlTree;
  }

  const urlOf = (result: boolean | UrlTree) =>
    TestBed.inject(Router).serializeUrl(result as UrlTree);

  it('lets an authenticated user through', () => {
    expect(runGuard('jwt')).toBe(true);
  });

  it('redirects a visitor to /login', () => {
    expect(urlOf(runGuard(null))).toBe('/login');
  });

  it('redirects to /login after logout', () => {
    expect(urlOf(runGuard('jwt', true))).toBe('/login');
  });
});
