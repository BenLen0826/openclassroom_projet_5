import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthResponse } from '../models/auth';
import { AuthService } from '../services/auth.service';
import { AuthStore } from '../stores/auth.store';
import { Header } from './header';

@Component({ template: '' })
class Empty {}

describe('Header', () => {
  // jsdom n'implémente pas showModal() / close() de <dialog> : remplacés le temps des tests
  const originalShowModal = HTMLDialogElement.prototype.showModal;
  const originalClose = HTMLDialogElement.prototype.close;
  // jsdom n'implémente pas non plus matchMedia : on garde l'écouteur pour simuler un changement de largeur
  let mediaListeners: ((event: { matches: boolean }) => void)[] = [];

  beforeEach(() => {
    mediaListeners = [];
    vi.stubGlobal('matchMedia', (query: string) => ({
      media: query,
      matches: false,
      addEventListener: (_: string, listener: (event: { matches: boolean }) => void) =>
        mediaListeners.push(listener),
      removeEventListener: vi.fn(),
    }));
    HTMLDialogElement.prototype.showModal = vi.fn(function (this: HTMLDialogElement) {
      this.open = true;
    });
    HTMLDialogElement.prototype.close = vi.fn(function (this: HTMLDialogElement) {
      this.open = false;
      this.dispatchEvent(new Event('close'));
    });
  });

  afterEach(() => {
    HTMLDialogElement.prototype.showModal = originalShowModal;
    HTMLDialogElement.prototype.close = originalClose;
    vi.unstubAllGlobals();
  });

  /** Crée l'en-tête sur la page donnée, connecté par défaut (token `null` : visiteur). */
  async function render(url = '/posts', token: string | null = 'jwt') {
    localStorage.clear();
    if (token) {
      localStorage.setItem('mdd_token', token);
    }
    TestBed.configureTestingModule({
      imports: [Header],
      providers: [
        provideRouter([
          { path: '', component: Empty },
          { path: 'posts', component: Empty },
          { path: 'topics', component: Empty },
          { path: 'profile', component: Empty },
          { path: 'login', component: Empty },
          { path: 'register', component: Empty },
        ]),
        {
          provide: AuthService,
          useValue: { login: () => of<AuthResponse>({ token: 'new-jwt' }) },
        },
      ],
    });
    await TestBed.inject(Router).navigateByUrl(url);
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
    const page = fixture.nativeElement as HTMLElement;
    const dialog = page.querySelector('dialog')!;
    const burger = page.querySelector<HTMLButtonElement>('button[aria-label="Ouvrir le menu"]')!;
    return { fixture, page, dialog, burger };
  }

  it('shows the desktop links with their destinations', async () => {
    const { page } = await render();
    const nav = page.querySelector('.header__nav')!;

    expect(nav.querySelector('a[href="/posts"]')?.textContent?.trim()).toBe('Articles');
    expect(nav.querySelector('a[href="/topics"]')?.textContent?.trim()).toBe('Thèmes');
    expect(nav.querySelector('a[href="/profile"]')?.getAttribute('aria-label')).toBe('Profil');
    expect(nav.querySelector('button')?.textContent?.trim()).toBe('Se déconnecter');
    expect(page.querySelector('img')?.getAttribute('alt')).toBe('Logo MDD');
  });

  it('marks the active link with aria-current', async () => {
    const { page } = await render('/topics');
    const nav = page.querySelector('.header__nav')!;

    expect(nav.querySelector('a[href="/topics"]')?.getAttribute('aria-current')).toBe('page');
    expect(nav.querySelector('a[href="/posts"]')?.hasAttribute('aria-current')).toBe(false);
  });

  it('logout clears the token and goes back to the home page', async () => {
    // Arrange
    const { page, fixture } = await render();

    // Act
    page.querySelector<HTMLButtonElement>('.header__nav button')!.click();
    await fixture.whenStable();

    // Assert
    expect(localStorage.getItem('mdd_token')).toBeNull();
    expect(TestBed.inject(Router).url).toBe('/');
  });

  it('opens the mobile menu from the burger button', async () => {
    // Arrange
    const { fixture, dialog, burger } = await render();
    expect(burger.getAttribute('aria-expanded')).toBe('false');

    // Act
    burger.click();
    await fixture.whenStable();

    // Assert
    expect(dialog.open).toBe(true);
    expect(burger.getAttribute('aria-expanded')).toBe('true');
    expect(dialog.querySelector('a[href="/topics"]')?.textContent?.trim()).toBe('Thèmes');
  });

  it('closes the mobile menu when a link is clicked', async () => {
    // Arrange
    const { fixture, dialog, burger } = await render();
    burger.click();
    await fixture.whenStable();

    // Act
    dialog.querySelector<HTMLAnchorElement>('a[href="/topics"]')!.click();
    await fixture.whenStable();

    // Assert
    expect(dialog.open).toBe(false);
    expect(burger.getAttribute('aria-expanded')).toBe('false');
    expect(TestBed.inject(Router).url).toBe('/topics');
  });

  it('closes the mobile menu and logs out from it', async () => {
    // Arrange
    const { fixture, dialog, burger } = await render();
    burger.click();
    await fixture.whenStable();

    // Act
    dialog.querySelector<HTMLButtonElement>('button.header__logout')!.click();
    await fixture.whenStable();

    // Assert
    expect(dialog.open).toBe(false);
    expect(localStorage.getItem('mdd_token')).toBeNull();
    expect(TestBed.inject(Router).url).toBe('/');
  });

  it('closes the mobile menu when the backdrop is clicked', async () => {
    // Arrange
    const { fixture, dialog, burger } = await render();
    burger.click();
    await fixture.whenStable();

    // Act : un clic sur le fond a pour cible le <dialog> lui-même
    dialog.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    await fixture.whenStable();

    // Assert
    expect(dialog.open).toBe(false);
  });

  it('closes the mobile menu when the screen becomes desktop wide', async () => {
    // Arrange : menu ouvert sur un téléphone
    const { fixture, dialog, burger } = await render();
    burger.click();
    await fixture.whenStable();

    // Act : passage en paysage, au-delà du point de rupture
    mediaListeners.forEach((listener) => listener({ matches: true }));
    await fixture.whenStable();

    // Assert
    expect(dialog.open).toBe(false);
    expect(burger.getAttribute('aria-expanded')).toBe('false');
  });

  it('gives the initial focus of the menu to the panel, not to the logout button', async () => {
    const { dialog } = await render();

    // showModal() donne le focus à l'élément [autofocus] du <dialog>
    const autofocused = dialog.querySelectorAll('[autofocus]');
    expect(autofocused.length).toBe(1);
    expect(autofocused[0].classList.contains('header__menu-panel')).toBe(true);
  });

  it('hides the icons from screen readers and labels the burger', async () => {
    const { page, burger } = await render();

    const icons = page.querySelectorAll('.material-icons-outlined');
    expect(icons.length).toBeGreaterThan(0);
    icons.forEach((icon) => expect(icon.getAttribute('aria-hidden')).toBe('true'));
    expect(burger.querySelector('.material-icons-outlined')?.textContent?.trim()).toBe('menu');
  });

  describe('for a visitor', () => {
    it('shows nothing on the home page', async () => {
      const { page } = await render('/', null);

      expect(page.querySelector('header')).toBeNull();
      expect(page.querySelector('dialog')).toBeNull();
    });

    it('shows the logo only on the login and register pages', async () => {
      const { fixture, page } = await render('/login', null);
      expectLogoOnly(page);

      await TestBed.inject(Router).navigateByUrl('/register');
      await fixture.whenStable();
      expectLogoOnly(page);
    });

    it('keeps the logo only when the URL has a query or a fragment', async () => {
      const { fixture, page } = await render('/login?x=1', null);
      expectLogoOnly(page);

      await TestBed.inject(Router).navigateByUrl('/register#a');
      await fixture.whenStable();
      expectLogoOnly(page);
    });

    it('switches to the full header as soon as the user logs in', async () => {
      // Arrange
      const { fixture, page } = await render('/login', null);

      // Act : connexion sans recréer le composant
      TestBed.inject(AuthStore).login({ identifier: 'user1', password: 'Demo1234!' }).subscribe();
      await fixture.whenStable();

      // Assert
      expect(page.querySelector('.header__nav a[href="/topics"]')).not.toBeNull();
    });

    it('shows nothing after logout (back on the home page)', async () => {
      // Arrange
      const { fixture, page } = await render();

      // Act
      page.querySelector<HTMLButtonElement>('.header__nav .header__logout')!.click();
      await fixture.whenStable();

      // Assert
      expect(page.querySelector('header')).toBeNull();
    });

    /** En-tête réduit au logo (non cliquable), sans liens ni menu. */
    function expectLogoOnly(page: HTMLElement): void {
      expect(page.querySelector('header img')?.getAttribute('alt')).toBe('Logo MDD');
      expect(page.querySelector('header a, header button')).toBeNull();
      expect(page.querySelector('dialog')).toBeNull();
    }
  });
});
