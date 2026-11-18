import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { TopicCard } from './topic-card';

/** Page de test : la carte reçoit un thème et un bouton projeté. */
@Component({
  imports: [TopicCard],
  template: `<app-topic-card [topic]="topic"
    ><button type="button">Action</button></app-topic-card
  >`,
})
class Host {
  topic = { id: 1, name: 'Angular', description: 'Front-end', subscribed: false };
}

describe('TopicCard', () => {
  it('shows the name, the description and the projected button', async () => {
    TestBed.configureTestingModule({ imports: [Host] });
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const card = fixture.nativeElement as HTMLElement;

    expect(card.querySelector('h2')?.textContent).toContain('Angular');
    expect(card.querySelector('p')?.textContent).toContain('Front-end');
    expect(card.querySelector('button')?.textContent).toBe('Action');
  });
});
