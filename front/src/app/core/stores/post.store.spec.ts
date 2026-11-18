import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { Post } from '../models/post';
import { AuthService } from '../services/auth.service';
import { PostService } from '../services/post.service';
import { AuthStore } from './auth.store';
import { PostStore } from './post.store';

describe('PostStore', () => {
  /** Article minimal ; l'API renvoie le fil du plus récent au plus ancien. */
  const post = (id: number, createdAt: string): Post => ({
    id,
    title: `Article ${id}`,
    content: 'Contenu',
    createdAt,
    authorUsername: 'bob',
    topicId: 3,
    topicName: 'Java',
  });
  const feed = [
    post(3, '2026-11-18T12:00:00Z'),
    post(2, '2026-11-17T12:00:00Z'),
    post(1, '2026-11-16T12:00:00Z'),
  ];
  let postService: { getFeed: ReturnType<typeof vi.fn> };
  let store: PostStore;

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('mdd_token', 'token-a');
    postService = { getFeed: vi.fn().mockReturnValue(of(feed)) };
    TestBed.configureTestingModule({
      providers: [
        { provide: PostService, useValue: postService },
        { provide: AuthService, useValue: { login: () => of({ token: 'token-b' }) } },
      ],
    });
    store = TestBed.inject(PostStore);
  });

  const ids = () => store.posts().map((p) => p.id);

  it('starts empty, newest first, not loading', () => {
    expect(store.posts()).toEqual([]);
    expect(store.order()).toBe('desc');
    expect(store.loading()).toBe(false);
  });

  it('load keeps the newest first', () => {
    store.load();

    expect(ids()).toEqual([3, 2, 1]);
  });

  it('toggleOrder reverses the list without a new request', () => {
    store.load();

    store.toggleOrder();

    expect(store.order()).toBe('asc');
    expect(ids()).toEqual([1, 2, 3]);
    expect(postService.getFeed).toHaveBeenCalledTimes(1);

    store.toggleOrder();
    expect(ids()).toEqual([3, 2, 1]);
  });

  it('the chosen order applies to a list loaded afterwards', () => {
    // Arrange : l'utilisateur inverse le tri avant la réponse de l'API
    const response = new Subject<Post[]>();
    postService.getFeed.mockReturnValue(response);
    store.load();
    store.toggleOrder();

    // Act
    response.next(feed);
    response.complete();

    // Assert
    expect(ids()).toEqual([1, 2, 3]);
  });

  it('load exposes an error message when the API fails', () => {
    postService.getFeed.mockReturnValue(throwError(() => new Error('API indisponible')));

    store.load();

    expect(store.error()).toBe('Impossible de charger les articles. Réessayez plus tard.');
    expect(store.loading()).toBe(false);
  });

  it("hides the previous user's feed when the token changes", () => {
    store.load();
    const authStore = TestBed.inject(AuthStore);

    authStore.logout();
    const afterLogout = store.posts();
    authStore.login({ identifier: 'b', password: 'x' }).subscribe();

    expect(afterLogout).toEqual([]);
    expect(store.posts()).toEqual([]);
  });
});
