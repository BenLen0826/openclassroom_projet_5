import { Component, input } from '@angular/core';
import { Topic } from '../../core/models/topic';

/**
 * Carte d'un thème, partagée par la page des thèmes et le profil (Annexe A.3) :
 * seul le bouton change, il est fourni par la page (projection de contenu).
 * <p>
 * Le niveau du titre dépend de la page : `h2` sous le titre de la page des thèmes,
 * `h3` sous la section « Abonnements » du profil. Le style vient d'une classe,
 * la carte a donc le même aspect quel que soit le niveau.
 */
@Component({
  selector: 'app-topic-card',
  templateUrl: './topic-card.html',
  styleUrl: './topic-card.scss',
})
export class TopicCard {
  readonly topic = input.required<Topic>();
  readonly headingLevel = input<2 | 3>(2);
}
