import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';
import { Topic } from '../../core/models/topic';
import { User } from '../../core/models/user';
import { SubscriptionService } from '../../core/services/subscription.service';
import { TopicService } from '../../core/services/topic.service';
import { UserService } from '../../core/services/user.service';
import { PASSWORD_RULE_MESSAGE } from '../../shared/validators/password.validator';
import { Profile } from './profile';

describe('Profile', () => {
  const bob: User = { id: 7, username: 'bob', email: 'bob@mail.fr' };
  const topics: Topic[] = [
    { id: 1, name: 'Angular', description: 'Front-end', subscribed: false },
    { id: 2, name: 'Java', description: 'Back-end', subscribed: true },
  ];
  let subscriptionService: { unsubscribe: ReturnType<typeof vi.fn> };
  let userService: { getMe: ReturnType<typeof vi.fn>; updateMe: ReturnType<typeof vi.fn> };
  let fixture: ComponentFixture<Profile>;
  let page: HTMLElement;

  /** Crée la page avec le profil et les thèmes renvoyés par l'API. */
  async function render(options: { me?: Observable<User>; topics?: Topic[] } = {}): Promise<void> {
    localStorage.clear();
    localStorage.setItem('mdd_token', 'jwt');
    subscriptionService = { unsubscribe: vi.fn().mockReturnValue(of(undefined)) };
    userService = { getMe: vi.fn().mockReturnValue(options.me ?? of(bob)), updateMe: vi.fn() };
    TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        { provide: TopicService, useValue: { getAll: () => of(options.topics ?? topics) } },
        { provide: SubscriptionService, useValue: subscriptionService },
        { provide: UserService, useValue: userService },
      ],
    });
    fixture = TestBed.createComponent(Profile);
    page = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  }

  const input = (name: string) => page.querySelector<HTMLInputElement>(`input[name="${name}"]`)!;
  const saveButton = () => page.querySelector<HTMLButtonElement>('button[type="submit"]')!;
  const errorUnder = (name: string) => page.querySelector(`#${name}-error`)?.textContent?.trim();

  async function type(name: string, value: string): Promise<void> {
    input(name).value = value;
    input(name).dispatchEvent(new Event('input'));
    input(name).dispatchEvent(new Event('blur'));
    await fixture.whenStable();
  }

  async function save(response: Observable<User>): Promise<void> {
    userService.updateMe.mockReturnValue(response);
    saveButton().click();
    await fixture.whenStable();
  }

  // --- Informations du profil ---

  it('prefills the form with the current profile, password empty', async () => {
    await render();

    expect(input('username').value).toBe('bob');
    expect(input('email').value).toBe('bob@mail.fr');
    expect(input('password').value).toBe('');
    expect(saveButton().disabled).toBe(false);
  });

  it('sends the profile without password when the field is empty', async () => {
    await render();
    await type('username', 'bobby');

    await save(of({ ...bob, username: 'bobby' }));

    expect(userService.updateMe).toHaveBeenCalledWith({
      username: 'bobby',
      email: 'bob@mail.fr',
      password: '',
    });
  });

  it('shows the success message, clears the password and shows the saved values', async () => {
    await render();
    await type('email', '  BOB2@Mail.FR ');
    await type('password', 'Nouveau1!');

    await save(of({ ...bob, email: 'bob2@mail.fr' }));

    expect(page.querySelector('[role="status"]')?.textContent?.trim()).toBe('Profil mis à jour.');
    expect(input('password').value).toBe('');
    expect(input('email').value).toBe('bob2@mail.fr');
  });

  it('removes the success message when a new save fails', async () => {
    // Arrange : premier enregistrement réussi
    await render();
    await save(of(bob));
    expect(page.querySelector('[role="status"]')).not.toBeNull();

    // Act : nouvel enregistrement sans rien modifier, qui échoue
    await save(throwError(() => new HttpErrorResponse({ status: 500 })));

    // Assert : seul le message d'erreur reste
    expect(page.querySelector('[role="status"]')).toBeNull();
    expect(page.querySelector('[role="alert"]')?.textContent).toContain('Une erreur est survenue.');
  });

  it('shows a 409 under the email field', async () => {
    await render();

    await save(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { detail: 'Cet e-mail est déjà utilisé.', field: 'email' },
          }),
      ),
    );

    expect(errorUnder('email')).toBe('Cet e-mail est déjà utilisé.');
    expect(page.querySelector('[role="status"]')).toBeNull();
  });

  it('applies the password rule only when a password is typed', async () => {
    await render();
    await type('password', '');
    expect(errorUnder('password')).toBeUndefined();
    expect(saveButton().disabled).toBe(false);

    await type('password', 'abc');

    expect(errorUnder('password')).toBe(PASSWORD_RULE_MESSAGE);
    expect(saveButton().disabled).toBe(true);
  });

  it('disables the button while saving (no double submission)', async () => {
    await render();

    await save(new Subject<User>());
    saveButton().click();

    expect(saveButton().disabled).toBe(true);
    expect(userService.updateMe).toHaveBeenCalledTimes(1);
  });

  it('shows an error instead of the form when the profile cannot be loaded', async () => {
    await render({ me: throwError(() => new HttpErrorResponse({ status: 500 })) });

    expect(page.querySelector('form')).toBeNull();
    expect(page.querySelector('[role="alert"]')?.textContent).toContain(
      'Impossible de charger le profil.',
    );
  });

  // --- Abonnements ---

  it('lists only the followed topics with "Se désabonner"', async () => {
    await render();

    const cards = page.querySelectorAll('app-topic-card');
    expect(cards.length).toBe(1);
    expect(cards[0].querySelector('h2')?.textContent).toContain('Java');
    expect(cards[0].querySelector('button')?.textContent?.trim()).toBe('Se désabonner');
  });

  it('unsubscribing removes the card', async () => {
    await render();

    page.querySelector<HTMLButtonElement>('app-topic-card button')!.click();
    await fixture.whenStable();

    expect(subscriptionService.unsubscribe).toHaveBeenCalledWith(2);
    expect(page.querySelector('app-topic-card')).toBeNull();
    expect(page.textContent).toContain("Vous n'êtes abonné à aucun thème.");
  });

  it('shows a message when no topic is followed', async () => {
    await render({ topics: [topics[0]] });

    expect(page.textContent).toContain("Vous n'êtes abonné à aucun thème.");
  });
});
