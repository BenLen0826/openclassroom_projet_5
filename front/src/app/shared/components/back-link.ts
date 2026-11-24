import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Flèche « retour » des maquettes (Annexe A.3). Mène à une destination fixe fournie par la
 * page plutôt qu'à l'historique du navigateur : arrivé par un lien direct, l'utilisateur
 * n'a pas d'historique et le bouton ne mènerait nulle part.
 */
@Component({
  selector: 'app-back-link',
  imports: [RouterLink],
  templateUrl: './back-link.html',
  styleUrl: './back-link.scss',
})
export class BackLink {
  readonly to = input.required<string>();
}
