import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { AuthResponse } from '../../core/models/auth';
import { AuthService } from '../../core/services/auth.service';
import { PASSWORD_RULE_MESSAGE } from '../../shared/validators/password.validator';
import { Register } from './register';

describe('Register', () => {
  let fixture: ComponentFixture<Register>;
  let page: HTMLElement;
  let authService: { register: ReturnType<typeof vi.fn> };
  let navigateByUrl: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    localStorage.clear();
    authService = { register: vi.fn() };
    TestBed.configureTestingModule({
      imports: [Register],
      providers: [provideRouter([]), { provide: AuthService, useValue: authService }],
    });
    navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    fixture = TestBed.createComponent(Register);
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

  async function fillValidForm(): Promise<void> {
    await type('username', 'bob');
    await type('email', 'bob@mail.fr');
    await type('password', 'Secret1!');
  }

  async function submitWith(response: Observable<AuthResponse>): Promise<void> {
    authService.register.mockReturnValue(response);
    await fillValidForm();
    page.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    await fixture.whenStable();
  }

  /** Texte affiché sous un champ (élément `#<champ>-error`). */
  const errorUnder = (name: string) => page.querySelector(`#${name}-error`)?.textContent?.trim();

  const httpError = (status: number, error: unknown) =>
    throwError(() => new HttpErrorResponse({ status, error }));

  it('disables the submit button while the form is invalid', async () => {
    const button = page.querySelector<HTMLButtonElement>('button[type="submit"]')!;
    expect(button.disabled).toBe(true);

    await fillValidForm();

    expect(button.disabled).toBe(false);
  });

  it('shows the password rule under the field when the password is too weak', async () => {
    await type('password', 'abc');

    expect(errorUnder('password')).toBe(PASSWORD_RULE_MESSAGE);
  });

  it('navigates to /topics after a successful registration', async () => {
    await submitWith(of({ token: 'jwt' }));

    expect(authService.register).toHaveBeenCalledWith({
      username: 'bob',
      email: 'bob@mail.fr',
      password: 'Secret1!',
    });
    expect(localStorage.getItem('mdd_token')).toBe('jwt');
    expect(navigateByUrl).toHaveBeenCalledWith('/topics');
  });

  it('shows a 409 message under the email field', async () => {
    await submitWith(httpError(409, { detail: 'Cet e-mail est déjà utilisé.', field: 'email' }));

    expect(errorUnder('email')).toBe('Cet e-mail est déjà utilisé.');
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('removes the server message when the user edits the field', async () => {
    await submitWith(httpError(409, { detail: 'Cet e-mail est déjà utilisé.', field: 'email' }));

    await type('email', 'alice@mail.fr');

    expect(errorUnder('email')).toBeUndefined();
  });

  it('shows backend field errors under the matching fields', async () => {
    await submitWith(httpError(400, { errors: { username: 'Le nom est refusé par le serveur.' } }));

    expect(errorUnder('username')).toBe('Le nom est refusé par le serveur.');
  });

  it('removes the general message as soon as the user edits a field', async () => {
    // Arrange : erreur inattendue, message général affiché
    await submitWith(httpError(500, null));
    expect(page.querySelector('[role="alert"]')).not.toBeNull();

    // Act
    await type('username', 'bobby');

    // Assert
    expect(page.querySelector('[role="alert"]')).toBeNull();
  });

  it('shows a general message on an unexpected error', async () => {
    await submitWith(httpError(500, null));

    expect(page.querySelector('[role="alert"]')?.textContent).toContain(
      'Une erreur est survenue. Réessayez plus tard.',
    );
    expect(navigateByUrl).not.toHaveBeenCalled();
  });
});
