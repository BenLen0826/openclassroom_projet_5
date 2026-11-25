import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { TopicCard } from './topic-card';

const topic = { id: 1, name: 'Angular', description: 'Front-end', subscribed: false };

/** Page de test : la carte reçoit un thème et un bouton projeté (niveau de titre par défaut). */
@Component({
  imports: [TopicCard],
  template: `<app-topic-card [topic]="topic"
    ><button type="button">Action</button></app-topic-card
  >`,
})
class Host {
  topic = topic;
}

/** Page de test où la carte est sous un titre de niveau 2 (comme les abonnements du profil). */
@Component({
  imports: [TopicCard],
  template: `<app-topic-card [topic]="topic" [headingLevel]="3" />`,
})
class NestedHost {
  topic = topic;
}

describe('TopicCard', () => {
  async function render(host: typeof Host | typeof NestedHost): Promise<HTMLElement> {
    TestBed.configureTestingModule({ imports: [host] });
    const fixture = TestBed.createComponent(host);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows the name, the description and the projected button', async () => {
    const card = await render(Host);

    expect(card.querySelector('h2')?.textContent).toContain('Angular');
    expect(card.querySelector('p')?.textContent).toContain('Front-end');
    expect(card.querySelector('button')?.textContent).toBe('Action');
  });

  it('uses the heading level asked by the page, with the same look', async () => {
    const card = await render(NestedHost);

    const title = card.querySelector('h3');
    expect(title?.textContent).toContain('Angular');
    expect(card.querySelector('h2')).toBeNull();
    // Le style vient de la classe, pas de la balise : même aspect en h2 ou en h3
    expect(title?.classList.contains('topic-card__title')).toBe(true);
  });
});
