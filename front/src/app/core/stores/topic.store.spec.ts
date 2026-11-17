import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { Topic } from '../models/topic';
import { TopicService } from '../services/topic.service';
import { TopicStore } from './topic.store';

describe('TopicStore', () => {
  const topics: Topic[] = [
    { id: 1, name: 'Angular', description: 'Front-end' },
    { id: 2, name: 'Java', description: 'Back-end' },
  ];
  let topicService: { getAll: ReturnType<typeof vi.fn> };
  let store: TopicStore;

  beforeEach(() => {
    topicService = { getAll: vi.fn() };
    TestBed.configureTestingModule({
      providers: [{ provide: TopicService, useValue: topicService }],
    });
    store = TestBed.inject(TopicStore);
  });

  it('starts empty, not loading and without error', () => {
    expect(store.topics()).toEqual([]);
    expect(store.loading()).toBe(false);
    expect(store.error()).toBeNull();
  });

  it('load stores the topics returned by the API', () => {
    // Arrange
    topicService.getAll.mockReturnValue(of(topics));

    // Act
    store.load();

    // Assert
    expect(store.topics()).toEqual(topics);
    expect(store.loading()).toBe(false);
    expect(store.error()).toBeNull();
  });

  it('load is in loading state until the API answers', () => {
    // Arrange
    const response = new Subject<Topic[]>();
    topicService.getAll.mockReturnValue(response);

    // Act
    store.load();

    // Assert
    expect(store.loading()).toBe(true);
    response.next(topics);
    response.complete();
    expect(store.loading()).toBe(false);
  });

  it('load exposes an error message when the API fails', () => {
    // Arrange
    topicService.getAll.mockReturnValue(throwError(() => new Error('API indisponible')));

    // Act
    store.load();

    // Assert
    expect(store.error()).toBe('Impossible de charger les thèmes. Réessayez plus tard.');
    expect(store.loading()).toBe(false);
    expect(store.topics()).toEqual([]);
  });
});
