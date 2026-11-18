import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PostCard } from './post-card';

describe('PostCard', () => {
  it('shows the title linked to the post, the date, the author and the content', async () => {
    TestBed.configureTestingModule({ imports: [PostCard], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', {
      id: 42,
      title: 'Titre',
      content: 'Contenu',
      createdAt: '2026-11-18T12:00:00Z',
      authorUsername: 'bob',
      topicId: 3,
      topicName: 'Java',
    });
    await fixture.whenStable();
    const card = fixture.nativeElement as HTMLElement;

    expect(card.querySelector('a h2')?.textContent?.trim()).toBe('Titre');
    expect(card.querySelector('a')?.getAttribute('href')).toBe('/posts/42');
    expect(card.textContent).toContain('18/11/2026');
    expect(card.textContent).toContain('bob');
    expect(card.textContent).toContain('Contenu');
  });
});
