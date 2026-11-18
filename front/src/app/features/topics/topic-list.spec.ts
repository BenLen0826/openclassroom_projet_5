import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';
import { Topic } from '../../core/models/topic';
import { SubscriptionService } from '../../core/services/subscription.service';
import { TopicService } from '../../core/services/topic.service';
import { TopicList } from './topic-list';

describe('TopicList', () => {
  const topics: Topic[] = [
    { id: 1, name: 'Angular', description: 'Front-end', subscribed: false },
    { id: 2, name: 'Java', description: 'Back-end', subscribed: true },
  ];
  let topicService: { getAll: ReturnType<typeof vi.fn> };
  let subscriptionService: { subscribe: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('mdd_token', 'jwt');
    topicService = { getAll: vi.fn() };
    subscriptionService = { subscribe: vi.fn() };
    TestBed.configureTestingModule({
      imports: [TopicList],
      providers: [
        { provide: TopicService, useValue: topicService },
        { provide: SubscriptionService, useValue: subscriptionService },
      ],
    });
  });

  /** Crée la page avec la réponse donnée pour la liste des thèmes. */
  async function renderWith(
    response: Observable<Topic[]>,
  ): Promise<{ fixture: ComponentFixture<TopicList>; page: HTMLElement }> {
    topicService.getAll.mockReturnValue(response);
    const fixture = TestBed.createComponent(TopicList);
    await fixture.whenStable();
    return { fixture, page: fixture.nativeElement as HTMLElement };
  }

  /** Bouton de la carte d'un thème. */
  const buttonOf = (page: HTMLElement, name: string) =>
    [...page.querySelectorAll('app-topic-card')]
      .find((card) => card.querySelector('h2')?.textContent?.includes(name))
      ?.querySelector('button') as HTMLButtonElement;

  it('displays the name and description of each topic', async () => {
    const { page } = await renderWith(of(topics));

    const cards = page.querySelectorAll('app-topic-card');
    expect(cards.length).toBe(2);
    expect(cards[0].querySelector('h2')?.textContent).toContain('Angular');
    expect(cards[0].querySelector('p')?.textContent).toContain('Front-end');
  });

  it('shows "S\'abonner" for a topic not followed and calls subscribe', async () => {
    const { fixture, page } = await renderWith(of(topics));
    subscriptionService.subscribe.mockReturnValue(of(undefined));
    const button = buttonOf(page, 'Angular');
    expect(button.textContent?.trim()).toBe("S'abonner");
    expect(button.disabled).toBe(false);

    button.click();
    await fixture.whenStable();

    expect(subscriptionService.subscribe).toHaveBeenCalledWith(1);
    expect(buttonOf(page, 'Angular').textContent?.trim()).toBe('Déjà abonné');
    expect(buttonOf(page, 'Angular').disabled).toBe(true);
  });

  it('shows a disabled "Déjà abonné" for a followed topic', async () => {
    const { page } = await renderWith(of(topics));

    const button = buttonOf(page, 'Java');
    expect(button.textContent?.trim()).toBe('Déjà abonné');
    expect(button.disabled).toBe(true);
  });

  it('disables the button while the subscription is being sent', async () => {
    const { fixture, page } = await renderWith(of(topics));
    subscriptionService.subscribe.mockReturnValue(new Subject<void>());

    buttonOf(page, 'Angular').click();
    await fixture.whenStable();

    expect(buttonOf(page, 'Angular').disabled).toBe(true);
    expect(buttonOf(page, 'Angular').textContent?.trim()).toBe("S'abonner");
  });

  it('shows the action error', async () => {
    const { fixture, page } = await renderWith(of(topics));
    subscriptionService.subscribe.mockReturnValue(throwError(() => new Error('réseau')));

    buttonOf(page, 'Angular').click();
    await fixture.whenStable();

    expect(page.querySelector('[role="alert"]')?.textContent).toContain(
      "Impossible de modifier l'abonnement.",
    );
    expect(buttonOf(page, 'Angular').textContent?.trim()).toBe("S'abonner");
  });

  it('keeps the list visible while reloading', async () => {
    // Arrange : liste déjà chargée lors d'une première visite
    await renderWith(of(topics));

    // Act : nouvelle visite, le rechargement n'a pas encore répondu
    const { page } = await renderWith(new Subject<Topic[]>());

    // Assert
    expect(page.textContent).not.toContain('Chargement…');
    expect(page.querySelectorAll('app-topic-card').length).toBe(2);
  });

  it('displays a message when there is no topic', async () => {
    const { page } = await renderWith(of([]));

    expect(page.textContent).toContain('Aucun thème disponible.');
  });

  it('displays an error message when the API fails', async () => {
    const { page } = await renderWith(throwError(() => new Error('API indisponible')));

    expect(page.querySelector('[role="alert"]')?.textContent).toContain(
      'Impossible de charger les thèmes.',
    );
    expect(page.querySelector('app-topic-card')).toBeNull();
  });
});
