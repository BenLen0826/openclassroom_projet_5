import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
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

  it('disables the submit button while the form is invalid', async () => {
    const button = page.querySelector<HTMLButtonElement>('button[type="submit"]')!;
    expect(button.disabled).toBe(true);

    await type('identifier', 'bob');
    await type('password', 'Secret1!');

    expect(button.disabled).toBe(false);
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
    expect(page.querySelector<HTMLButtonElement>('button[type="submit"]')!.disabled).toBe(true);
  });

  it('navigates to /topics after a successful login', async () => {
    await submitWith(of({ token: 'jwt' }));

    expect(authService.login).toHaveBeenCalledWith({ identifier: 'bob', password: 'Secret1!' });
    expect(localStorage.getItem('mdd_token')).toBe('jwt');
    expect(navigateByUrl).toHaveBeenCalledWith('/topics');
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
