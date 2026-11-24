import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { Post } from '../../core/models/post';
import { CommentService } from '../../core/services/comment.service';
import { PostService } from '../../core/services/post.service';
import { PostDetail } from './post-detail';

/** Page d'un article ; le détail des commentaires est testé dans `comment-section.spec.ts`. */
describe('PostDetail', () => {
  const post: Post = {
    id: 42,
    title: 'Titre',
    content: 'Contenu complet',
    createdAt: '2026-11-18T12:00:00Z',
    authorUsername: 'bob',
    topicId: 2,
    topicName: 'Java',
  };
  let getById: ReturnType<typeof vi.fn>;
  let getForPost: ReturnType<typeof vi.fn>;

  async function renderWith(response: Observable<Post>): Promise<HTMLElement> {
    getById = vi.fn().mockReturnValue(response);
    getForPost = vi.fn().mockReturnValue(of([]));
    TestBed.configureTestingModule({
      imports: [PostDetail],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: { getById } },
        { provide: CommentService, useValue: { getForPost, create: vi.fn() } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '42' }) } },
        },
      ],
    });
    const fixture = TestBed.createComponent(PostDetail);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows the title, date, author, topic and content of the post', async () => {
    const page = await renderWith(of(post));

    expect(getById).toHaveBeenCalledWith(42);
    expect(page.querySelector('h1')?.textContent?.trim()).toBe('Titre');
    expect(page.textContent).toContain('18/11/2026');
    expect(page.textContent).toContain('bob');
    expect(page.textContent).toContain('Java');
    expect(page.textContent).toContain('Contenu complet');
  });

  it('links back to the feed', async () => {
    const page = await renderWith(of(post));

    expect(page.querySelector('a[aria-label="Retour"]')?.getAttribute('href')).toBe('/posts');
  });

  it('shows the comment section of this post', async () => {
    const page = await renderWith(of(post));

    expect(page.querySelector('app-comment-section')).not.toBeNull();
    expect(getForPost).toHaveBeenCalledWith(42);
  });

  it('shows a message and no comment section when the post does not exist', async () => {
    const page = await renderWith(throwError(() => new HttpErrorResponse({ status: 404 })));

    expect(page.querySelector('[role="alert"]')?.textContent?.trim()).toBe('Article introuvable.');
    expect(page.querySelector('h1')).toBeNull();
    expect(page.querySelector('app-comment-section')).toBeNull();
    expect(getForPost).not.toHaveBeenCalled();
  });
});
