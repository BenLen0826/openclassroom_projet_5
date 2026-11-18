import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { Topic } from '../models/topic';
import { AuthService } from '../services/auth.service';
import { SubscriptionService } from '../services/subscription.service';
import { TopicService } from '../services/topic.service';
import { AuthStore } from './auth.store';
import { TopicStore } from './topic.store';

describe('TopicStore', () => {
  const topics: Topic[] = [
    { id: 1, name: 'Angular', description: 'Front-end', subscribed: false },
    { id: 2, name: 'Java', description: 'Back-end', subscribed: true },
  ];
  let topicService: { getAll: ReturnType<typeof vi.fn> };
  let subscriptionService: {
    subscribe: ReturnType<typeof vi.fn>;
    unsubscribe: ReturnType<typeof vi.fn>;
  };
  let store: TopicStore;

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('mdd_token', 'token-a');
    topicService = { getAll: vi.fn().mockReturnValue(of(topics)) };
    subscriptionService = { subscribe: vi.fn(), unsubscribe: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        { provide: TopicService, useValue: topicService },
        { provide: SubscriptionService, useValue: subscriptionService },
        { provide: AuthService, useValue: { login: () => of({ token: 'token-b' }) } },
      ],
    });
    store = TestBed.inject(TopicStore);
  });

  const topic = (id: number) => store.topics().find((t) => t.id === id);

  it('starts empty, not loading and without error', () => {
    expect(store.topics()).toEqual([]);
    expect(store.loading()).toBe(false);
    expect(store.error()).toBeNull();
  });

  it('load stores the topics returned by the API', () => {
    store.load();

    expect(store.topics()).toEqual(topics);
    expect(store.loading()).toBe(false);
    expect(store.error()).toBeNull();
  });

  it('load is in loading state until the API answers', () => {
    const response = new Subject<Topic[]>();
    topicService.getAll.mockReturnValue(response);

    store.load();

    expect(store.loading()).toBe(true);
    response.next(topics);
    response.complete();
    expect(store.loading()).toBe(false);
  });

  it('load exposes an error message when the API fails', () => {
    topicService.getAll.mockReturnValue(throwError(() => new Error('API indisponible')));

    store.load();

    expect(store.error()).toBe('Impossible de charger les thèmes. Réessayez plus tard.');
    expect(store.loading()).toBe(false);
    expect(store.topics()).toEqual([]);
  });

  it('subscribedTopics only contains the followed topics', () => {
    store.load();

    expect(store.subscribedTopics().map((t) => t.name)).toEqual(['Java']);
  });

  it('subscribe marks the topic as subscribed', () => {
    store.load();
    subscriptionService.subscribe.mockReturnValue(of(undefined));

    store.subscribe(1);

    expect(subscriptionService.subscribe).toHaveBeenCalledWith(1);
    expect(topic(1)?.subscribed).toBe(true);
    expect(store.isPending(1)).toBe(false);
  });

  it('unsubscribe marks it as not subscribed and removes it from subscribedTopics', () => {
    store.load();
    subscriptionService.unsubscribe.mockReturnValue(of(undefined));

    store.unsubscribe(2);

    expect(topic(2)?.subscribed).toBe(false);
    expect(store.subscribedTopics()).toEqual([]);
  });

  it('ignores a second subscribe while the first is pending', () => {
    store.load();
    const response = new Subject<void>();
    subscriptionService.subscribe.mockReturnValue(response);

    store.subscribe(1);
    store.subscribe(1);

    expect(store.isPending(1)).toBe(true);
    expect(subscriptionService.subscribe).toHaveBeenCalledTimes(1);
    response.next();
    response.complete();
    expect(store.isPending(1)).toBe(false);
  });

  it('keeps the topic unsubscribed and exposes an error when the call fails', () => {
    store.load();
    subscriptionService.subscribe.mockReturnValue(throwError(() => new Error('réseau')));

    store.subscribe(1);

    expect(topic(1)?.subscribed).toBe(false);
    expect(store.isPending(1)).toBe(false);
    expect(store.actionError()).toBe("Impossible de modifier l'abonnement. Réessayez plus tard.");
  });

  it('clears the action error on the next load (other page, new login)', () => {
    // Arrange : un abonnement a échoué
    store.load();
    subscriptionService.subscribe.mockReturnValue(throwError(() => new Error('réseau')));
    store.subscribe(1);
    expect(store.actionError()).not.toBeNull();

    // Act : l'utilisateur ouvre une autre page qui recharge les thèmes
    store.load();

    // Assert
    expect(store.actionError()).toBeNull();
  });

  it("hides the previous user's topics when the token changes", () => {
    // Arrange : liste chargée pour le compte A
    store.load();
    expect(store.topics().length).toBe(2);
    const authStore = TestBed.inject(AuthStore);

    // Act : déconnexion, puis connexion avec le compte B (avant tout rechargement)
    authStore.logout();
    const afterLogout = store.topics();
    authStore.login({ identifier: 'b', password: 'x' }).subscribe();

    // Assert
    expect(afterLogout).toEqual([]);
    expect(authStore.token()).toBe('token-b');
    expect(store.topics()).toEqual([]);
    expect(store.subscribedTopics()).toEqual([]);
  });
});
