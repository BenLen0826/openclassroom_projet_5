import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthStore } from '../stores/auth.store';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  const topicsUrl = `${environment.apiUrl}/topics`;
  const loginUrl = `${environment.apiUrl}/auth/login`;
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let navigateByUrl: ReturnType<typeof vi.spyOn>;

  /** Configure l'application, avec ou sans token déjà enregistré. */
  function setUp(token: string | null): void {
    localStorage.clear();
    if (token) {
      localStorage.setItem('mdd_token', token);
    }
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
  }

  afterEach(() => httpTesting.verify());

  it('adds the Bearer header to API calls when a token exists', () => {
    setUp('jwt');

    http.get(topicsUrl).subscribe();

    expect(httpTesting.expectOne(topicsUrl).request.headers.get('Authorization')).toBe(
      'Bearer jwt',
    );
  });

  it('sends no header without token', () => {
    setUp(null);

    http.get(topicsUrl).subscribe();

    expect(httpTesting.expectOne(topicsUrl).request.headers.has('Authorization')).toBe(false);
  });

  it('never sends the token to /auth/ requests', () => {
    // Un token périmé ferait refuser la connexion avant même le contrôleur
    setUp('expired-jwt');

    http.post(loginUrl, {}).subscribe();

    expect(httpTesting.expectOne(loginUrl).request.headers.has('Authorization')).toBe(false);
  });

  it('never sends the token to another domain', () => {
    setUp('jwt');

    http.get('https://exemple.fr/data').subscribe();

    expect(
      httpTesting.expectOne('https://exemple.fr/data').request.headers.has('Authorization'),
    ).toBe(false);
  });

  it('logs out and redirects to /login on a 401 from the API', () => {
    // Arrange : token expiré resté dans le navigateur
    setUp('expired-jwt');
    const error = vi.fn();

    // Act
    http.get(topicsUrl).subscribe({ error });
    httpTesting.expectOne(topicsUrl).flush(null, { status: 401, statusText: 'Unauthorized' });

    // Assert
    expect(TestBed.inject(AuthStore).isAuthenticated()).toBe(false);
    expect(localStorage.getItem('mdd_token')).toBeNull();
    expect(navigateByUrl).toHaveBeenCalledWith('/login');
    expect(error).toHaveBeenCalled();
  });

  it('ignores a 401 on /auth/login (the login page shows the message)', () => {
    setUp(null);
    const error = vi.fn();

    http.post(loginUrl, {}).subscribe({ error });
    httpTesting.expectOne(loginUrl).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(error).toHaveBeenCalled();
  });

  it('keeps the session on other errors', () => {
    setUp('jwt');

    http.get(topicsUrl).subscribe({ error: () => undefined });
    httpTesting.expectOne(topicsUrl).flush(null, { status: 500, statusText: 'Server Error' });

    expect(TestBed.inject(AuthStore).isAuthenticated()).toBe(true);
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('uses the current token: no header after logout', () => {
    setUp('jwt');
    TestBed.inject(AuthStore).logout();

    http.get(topicsUrl).subscribe();

    expect(httpTesting.expectOne(topicsUrl).request.headers.has('Authorization')).toBe(false);
  });
});
