import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';
import { AuthResponse } from '../../core/models/auth';
import { AuthService } from '../../core/services/auth.service';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let page: HTMLElement;
  let authService: { login: ReturnType<typeof vi.fn> };
  let navigateByUrl: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    localStorage.clear();
    authService = { login: vi.fn() };
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), { provide: AuthService, useValue: authService }],
    });
    navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    fixture = TestBed.createComponent(Login);
    page = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  /** Saisit une valeur dans un champ puis le quitte (champ « touché »). */
  async function type(name: string, value: string): Promise<void> {
    const input = page.querySelector<HTMLInputElement>(`input[name="${name}"]`)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new Event('blur'));
    await fixture.whenStable();
  }

  async function submitWith(response: Observable<AuthResponse>): Promise<void> {
    authService.login.mockReturnValue(response);
    await type('identifier', 'bob');
    await type('password', 'Secret1!');
    page.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    await fixture.whenStable();
  }

  const alertText = () => page.querySelector('[role="alert"]')?.textContent?.trim();

  const submitButton = () => page.querySelector<HTMLButtonElement>('button[type="submit"]')!;

  async function send(): Promise<void> {
    submitButton().click();
    await fixture.whenStable();
  }

  it('shows every message and calls nothing when an empty form is sent', async () => {
    // Act
    await send();

    // Assert
    expect(submitButton().disabled).toBe(false);
    expect(page.querySelector('#identifier-error')?.textContent?.trim()).toBe(
      "L'identifiant est obligatoire.",
    );
    expect(page.querySelector('#password-error')?.textContent?.trim()).toBe(
      'Le mot de passe est obligatoire.',
    );
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('sends only once on a double submission', async () => {
    // Arrange : la première requête ne répond pas encore
    authService.login.mockReturnValue(new Subject<AuthResponse>());
    await type('identifier', 'bob');
    await type('password', 'Secret1!');

    // Act
    await send();
    await send();

    // Assert
    expect(authService.login).toHaveBeenCalledTimes(1);
  });

  it('links back to the home page', () => {
    expect(page.querySelector('a[aria-label="Retour"]')?.getAttribute('href')).toBe('/');
  });

  it('shows the required message under a touched empty field', async () => {
    await type('identifier', '');

    expect(page.querySelector('#identifier-error')?.textContent?.trim()).toBe(
      "L'identifiant est obligatoire.",
    );
  });

  it('treats fields containing only spaces as empty (the API would refuse them)', async () => {
    await type('identifier', '   ');
    await type('password', '   ');

    expect(page.querySelector('#identifier-error')?.textContent?.trim()).toBe(
      "L'identifiant est obligatoire.",
    );
    expect(page.querySelector('#password-error')?.textContent?.trim()).toBe(
      'Le mot de passe est obligatoire.',
    );
    await send();
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('navigates to /posts after a successful login', async () => {
    await submitWith(of({ token: 'jwt' }));

    expect(authService.login).toHaveBeenCalledWith({ identifier: 'bob', password: 'Secret1!' });
    expect(localStorage.getItem('mdd_token')).toBe('jwt');
    expect(navigateByUrl).toHaveBeenCalledWith('/posts');
  });

  it('shows the 401 message from the API', async () => {
    await submitWith(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 401,
            error: { detail: 'Identifiant ou mot de passe incorrect.' },
          }),
      ),
    );

    expect(alertText()).toBe('Identifiant ou mot de passe incorrect.');
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('removes the error message as soon as the user edits a field', async () => {
    // Arrange : identifiants refusés, message affiché
    await submitWith(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 401,
            error: { detail: 'Identifiant ou mot de passe incorrect.' },
          }),
      ),
    );
    expect(alertText()).toBe('Identifiant ou mot de passe incorrect.');

    // Act : l'utilisateur corrige son mot de passe
    await type('password', 'Secret2!');

    // Assert
    expect(page.querySelector('[role="alert"]')).toBeNull();
  });

  it('shows a general message on an unexpected error', async () => {
    await submitWith(throwError(() => new HttpErrorResponse({ status: 500 })));

    expect(alertText()).toBe('Une erreur est survenue. Réessayez plus tard.');
  });
});
