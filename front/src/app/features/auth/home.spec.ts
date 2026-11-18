import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Home } from './home';

describe('Home', () => {
  it('links to the login and register pages', async () => {
    // Arrange + Act
    TestBed.configureTestingModule({ imports: [Home], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(Home);
    await fixture.whenStable();
    const page = fixture.nativeElement as HTMLElement;

    // Assert
    const login = page.querySelector<HTMLAnchorElement>('a[href="/login"]');
    const register = page.querySelector<HTMLAnchorElement>('a[href="/register"]');
    expect(login?.textContent?.trim()).toBe('Se connecter');
    expect(register?.textContent?.trim()).toBe("S'inscrire");
    expect(page.querySelector('img')?.getAttribute('alt')).toBe('Logo MDD');
  });
});
