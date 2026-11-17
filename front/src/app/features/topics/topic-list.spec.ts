import { TestBed } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';
import { Topic } from '../../core/models/topic';
import { TopicService } from '../../core/services/topic.service';
import { TopicList } from './topic-list';

describe('TopicList', () => {
  /** Crée la page avec un service HTTP simulé qui renvoie la réponse donnée. */
  async function renderWith(response: Observable<Topic[]>): Promise<HTMLElement> {
    TestBed.configureTestingModule({
      imports: [TopicList],
      providers: [{ provide: TopicService, useValue: { getAll: () => response } }],
    });
    const fixture = TestBed.createComponent(TopicList);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('displays the name and description of each topic', async () => {
    // Arrange + Act
    const page = await renderWith(
      of([
        { id: 1, name: 'Angular', description: 'Front-end' },
        { id: 2, name: 'Java', description: 'Back-end' },
      ]),
    );

    // Assert
    const items = page.querySelectorAll('li');
    expect(items.length).toBe(2);
    expect(items[0].querySelector('h2')?.textContent).toContain('Angular');
    expect(items[0].querySelector('p')?.textContent).toContain('Front-end');
    expect(items[1].querySelector('h2')?.textContent).toContain('Java');
  });

  it('displays a message when there is no topic', async () => {
    // Arrange + Act
    const page = await renderWith(of([]));

    // Assert
    expect(page.textContent).toContain('Aucun thème disponible.');
  });

  it('displays an error message when the API fails', async () => {
    // Arrange + Act
    const page = await renderWith(throwError(() => new Error('API indisponible')));

    // Assert
    const alert = page.querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('Impossible de charger les thèmes.');
    expect(page.querySelector('ul')).toBeNull();
  });
});
