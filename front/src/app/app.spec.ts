import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  /** Crée l'application, avec ou sans token déjà enregistré. */
  async function render(token: string | null): Promise<HTMLElement> {
    localStorage.clear();
    if (token) {
      localStorage.setItem('mdd_token', token);
    }
    TestBed.configureTestingModule({ imports: [App], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows no navigation to a visitor', async () => {
    const page = await render(null);

    expect(page.querySelector('nav')).toBeNull();
  });

  it('shows the navigation when authenticated', async () => {
    const page = await render('jwt');

    expect(page.querySelector('nav a[href="/topics"]')?.textContent?.trim()).toBe('Thèmes');
    expect(page.querySelector('nav a[href="/profile"]')?.textContent?.trim()).toBe('Profil');
    expect(page.querySelector('nav button')?.textContent?.trim()).toBe('Se déconnecter');
  });

  it('logout clears the token and goes back to the home page', async () => {
    // Arrange
    const page = await render('jwt');
    const navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);

    // Act
    page.querySelector<HTMLButtonElement>('nav button')!.click();
    await TestBed.inject(Router).navigated;

    // Assert
    expect(localStorage.getItem('mdd_token')).toBeNull();
    expect(navigateByUrl).toHaveBeenCalledWith('/');
  });
});
