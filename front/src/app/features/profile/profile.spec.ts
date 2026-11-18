import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Topic } from '../../core/models/topic';
import { SubscriptionService } from '../../core/services/subscription.service';
import { TopicService } from '../../core/services/topic.service';
import { Profile } from './profile';

describe('Profile', () => {
  let subscriptionService: { unsubscribe: ReturnType<typeof vi.fn> };

  /** Crée la page avec les thèmes renvoyés par l'API. */
  async function renderWith(
    topics: Topic[],
  ): Promise<{ fixture: ComponentFixture<Profile>; page: HTMLElement }> {
    localStorage.clear();
    localStorage.setItem('mdd_token', 'jwt');
    subscriptionService = { unsubscribe: vi.fn().mockReturnValue(of(undefined)) };
    TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        { provide: TopicService, useValue: { getAll: () => of(topics) } },
        { provide: SubscriptionService, useValue: subscriptionService },
      ],
    });
    const fixture = TestBed.createComponent(Profile);
    await fixture.whenStable();
    return { fixture, page: fixture.nativeElement as HTMLElement };
  }

  const topics: Topic[] = [
    { id: 1, name: 'Angular', description: 'Front-end', subscribed: false },
    { id: 2, name: 'Java', description: 'Back-end', subscribed: true },
  ];

  it('lists only the followed topics with "Se désabonner"', async () => {
    const { page } = await renderWith(topics);

    const cards = page.querySelectorAll('app-topic-card');
    expect(cards.length).toBe(1);
    expect(cards[0].querySelector('h2')?.textContent).toContain('Java');
    expect(cards[0].querySelector('button')?.textContent?.trim()).toBe('Se désabonner');
  });

  it('unsubscribing removes the card', async () => {
    const { fixture, page } = await renderWith(topics);

    page.querySelector<HTMLButtonElement>('app-topic-card button')!.click();
    await fixture.whenStable();

    expect(subscriptionService.unsubscribe).toHaveBeenCalledWith(2);
    expect(page.querySelector('app-topic-card')).toBeNull();
    expect(page.textContent).toContain("Vous n'êtes abonné à aucun thème.");
  });

  it('shows a message when no topic is followed', async () => {
    const { page } = await renderWith([topics[0]]);

    expect(page.textContent).toContain("Vous n'êtes abonné à aucun thème.");
  });
});
