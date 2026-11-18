import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { Post } from '../../core/models/post';
import { PostService } from '../../core/services/post.service';
import { PostList } from './post-list';

describe('PostList', () => {
  const post = (id: number, title: string, createdAt: string): Post => ({
    id,
    title,
    content: `Contenu de ${title}`,
    createdAt,
    authorUsername: 'bob',
    topicId: 3,
    topicName: 'Java',
  });
  // Midi UTC : même jour quel que soit le fuseau du navigateur en Europe
  const feed = [
    post(2, 'Récent', '2026-11-18T12:00:00Z'),
    post(1, 'Ancien', '2026-11-16T12:00:00Z'),
  ];
  let fixture: ComponentFixture<PostList>;
  let page: HTMLElement;

  async function renderWith(response: Observable<Post[]>): Promise<void> {
    localStorage.clear();
    localStorage.setItem('mdd_token', 'jwt');
    TestBed.configureTestingModule({
      imports: [PostList],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: { getFeed: () => response } },
      ],
    });
    fixture = TestBed.createComponent(PostList);
    page = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  }

  const titles = () => [...page.querySelectorAll('article h2')].map((h) => h.textContent?.trim());
  const sortButton = () => page.querySelector<HTMLButtonElement>('button')!;

  it('shows each post with its title, date, author and content, linked to its page', async () => {
    await renderWith(of(feed));

    const first = page.querySelector('article')!;
    expect(first.querySelector('h2')?.textContent?.trim()).toBe('Récent');
    expect(first.textContent).toContain('18/11/2026');
    expect(first.textContent).toContain('bob');
    expect(first.textContent).toContain('Contenu de Récent');
    expect(first.querySelector('a')?.getAttribute('href')).toBe('/posts/2');
  });

  it('links to the creation page', async () => {
    await renderWith(of(feed));

    const link = page.querySelector<HTMLAnchorElement>('a[href="/posts/new"]');
    expect(link?.textContent?.trim()).toBe('Créer un article');
  });

  it('reverses the order and the arrow when sorting', async () => {
    await renderWith(of(feed));
    expect(titles()).toEqual(['Récent', 'Ancien']);
    expect(sortButton().textContent?.trim()).toBe('Trier par date ↓');

    sortButton().click();
    await fixture.whenStable();

    expect(titles()).toEqual(['Ancien', 'Récent']);
    expect(sortButton().textContent?.trim()).toBe('Trier par date ↑');
  });

  it('shows a message when the feed is empty', async () => {
    await renderWith(of([]));

    expect(page.textContent).toContain(
      "Aucun article pour l'instant. Abonnez-vous à des thèmes pour remplir votre fil.",
    );
  });

  it('shows an error message when the API fails', async () => {
    await renderWith(throwError(() => new Error('API indisponible')));

    expect(page.querySelector('[role="alert"]')?.textContent).toContain(
      'Impossible de charger les articles.',
    );
  });
});
