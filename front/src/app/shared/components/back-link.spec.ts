import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { BackLink } from './back-link';

describe('BackLink', () => {
  async function render(to: string): Promise<HTMLElement> {
    TestBed.configureTestingModule({ imports: [BackLink], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(BackLink);
    fixture.componentRef.setInput('to', to);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('links to the given destination', async () => {
    const page = await render('/posts');

    expect(page.querySelector('a')?.getAttribute('href')).toBe('/posts');
  });

  it('is announced as "Retour" and hides the icon from screen readers', async () => {
    const page = await render('/');

    expect(page.querySelector('a')?.getAttribute('aria-label')).toBe('Retour');
    const icon = page.querySelector('.material-icons-outlined');
    expect(icon?.textContent?.trim()).toBe('arrow_back');
    expect(icon?.getAttribute('aria-hidden')).toBe('true');
  });
});
