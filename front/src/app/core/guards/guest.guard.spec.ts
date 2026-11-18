import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { guestGuard } from './guest.guard';

describe('guestGuard', () => {
  /** Exécute le guard, avec ou sans token déjà enregistré. */
  function runGuard(token: string | null): boolean | UrlTree {
    localStorage.clear();
    if (token) {
      localStorage.setItem('mdd_token', token);
    }
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    return TestBed.runInInjectionContext(() =>
      guestGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    ) as boolean | UrlTree;
  }

  it('lets a visitor through', () => {
    expect(runGuard(null)).toBe(true);
  });

  it('redirects an authenticated user to /posts', () => {
    const result = runGuard('jwt');

    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/posts');
  });
});
