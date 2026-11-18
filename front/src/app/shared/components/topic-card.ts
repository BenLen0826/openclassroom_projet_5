import { Component, input } from '@angular/core';
import { Topic } from '../../core/models/topic';

/**
 * Carte d'un thème, partagée par la page des thèmes et le profil (Annexe A.3) :
 * seul le bouton change, il est fourni par la page (projection de contenu).
 */
@Component({
  selector: 'app-topic-card',
  templateUrl: './topic-card.html',
})
export class TopicCard {
  readonly topic = input.required<Topic>();
}
